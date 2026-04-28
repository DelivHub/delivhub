package com.sparta.delivhub.domain.auth.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.auth.dto.*;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import com.sparta.delivhub.security.JwtTokenProvider;
import com.sparta.delivhub.security.TokenService;
import com.sparta.delivhub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // username(=user_id) 중복 검증
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .userRole(request.getRole())
                .build();

        User saved = userRepository.save(user);

        return SignupResponse.from(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 아이디 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 탈퇴한 계정 확인
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // accessToken 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUsername(),
                List.of(() -> "ROLE_" + user.getUserRole().name())
        );

        // refreshToken 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        // refreshToken 기한 설정
        long refreshExpiration = jwtTokenProvider.getRemainingExpiration(refreshToken);

        // refreshToken 저장
        tokenService.saveRefreshToken(user.getUsername(), refreshToken, refreshExpiration);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getUserRole())
                .build();
    }

    @Transactional
    public void logout(String username) {
        tokenService.deleteRefreshToken(username);
    }

    @Transactional
    public ReissueResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        // Redis 에 저장된 RT와 비교 (중복 로그인 시 이전 RT 무효화)
        String storedRT = tokenService.getRefreshToken(username);
        if (storedRT == null || !storedRT.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새 AT 발급
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String newAccessToken = jwtTokenProvider.createAccessToken(
                username, userDetails.getAuthorities());

        return ReissueResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

}
