package com.sparta.delivhub.domain.auth.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.auth.dto.LoginRequest;
import com.sparta.delivhub.domain.auth.dto.LoginResponse;
import com.sparta.delivhub.domain.auth.dto.SignupRequest;
import com.sparta.delivhub.domain.auth.dto.SignupResponse;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import com.sparta.delivhub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        String token = jwtTokenProvider.createAccessToken(
                user.getUsername(),
                List.of(() -> "ROLE_" + user.getUserRole().name())
        );

        return LoginResponse.builder()
                .accessToken(token)
                .username(user.getUsername())
                .role(user.getUserRole())
                .build();
    }
}
