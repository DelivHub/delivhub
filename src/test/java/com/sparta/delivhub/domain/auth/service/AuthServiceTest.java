package com.sparta.delivhub.domain.auth.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.auth.dto.*;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import com.sparta.delivhub.security.JwtTokenProvider;
import com.sparta.delivhub.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user01")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("회원가입_성공")
    void signup_success() {
        //given
        SignupRequest request = SignupRequest.builder()
                .username("user01")
                .password("Password1!")
                .nickname("홍길동")
                .email("user01@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        given(userRepository.existsByUsername("user01")).willReturn(false);
        given(userRepository.existsByEmail("user01@example.com")).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        //when
        SignupResponse response = authService.signup(request);

        //then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getEmail()).isEqualTo("user01@example.com");
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입_실패_중복_username")
    void signup_fail_duplicateUsername() {
        //given
        SignupRequest request = SignupRequest.builder()
                .username("user01")
                .password("Password1!")
                .nickname("홍길동")
                .email("user01@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        given(userRepository.existsByUsername("user01")).willReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_USERNAME);
    }

    @Test
    @DisplayName("회원가입_실패_중복_email")
    void signup_fail_duplicateEmail() {
        //given
        SignupRequest request = SignupRequest.builder()
                .username("user01")
                .password("Password1!")
                .nickname("홍길동")
                .email("user01@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        given(userRepository.existsByEmail("user01@example.com")).willReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인_성공")
    void loginSuccess() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("user01")
                .password("Password1!")
                .build();

        given(userRepository.findByUsername("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Password1!", user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(eq("user01"), anyList())).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken("user01")).willReturn("refresh-token");
        given(jwtTokenProvider.getRemainingExpiration("refresh-token")).willReturn(604800000L);

        //when
        LoginResponse response = authService.login(request);

        //then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        verify(tokenService).saveRefreshToken(eq("user01"), eq("refresh-token"), anyLong());
    }

    @Test
    @DisplayName("로그인_실패_존재X_유저")
    void login_fail_userNotFound() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("user01")
                .password("Password1!")
                .build();

        given(userRepository.findByUsername("user01")).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인_실패_탈퇴_계정")
    void login_fail_deactivatedAccount() {
        //given
        user.softDelete("admin");

        LoginRequest request = LoginRequest.builder()
                .username("user01")
                .password("Password1!")
                .build();

        given(userRepository.findByUsername("user01")).willReturn(Optional.of(user));

        //when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEACTIVATED_ACCOUNT);
    }

    @Test
    @DisplayName("로그인_실패_비밀번호_불일치")
    void login_fail_wrongPassword() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("user01")
                .password("Password1!")
                .build();

        given(userRepository.findByUsername("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Password1!", user.getPassword())).willReturn(false);
        //when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그아웃_성공")
    void logout_success() {
        // when
        authService.logout("user01");

        // then
        verify(tokenService).deleteRefreshToken("user01");
    }

    @Test
    @DisplayName("AT_재발급_성공")
    void reissue_success() {
        // given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(true);
        given(jwtTokenProvider.getUsername("refresh-token")).willReturn("user01");
        given(tokenService.getRefreshToken("user01")).willReturn("refresh-token");
        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(eq("user01"), anyCollection())).willReturn("new-access-token");

        // when
        ReissueResponse response = authService.reissue("refresh-token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("AT_재발급_실패_유효하지않은_RT")
    void reissue_fail_invalidToken() {
        // given
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.reissue("invalid-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("AT_재발급_실패_RT_없음_로그아웃_상태_RT_삭제")
    void reissue_fail_tokenNotFound() {
        // given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(true);
        given(jwtTokenProvider.getUsername("refresh-token")).willReturn("user01");
        given(tokenService.getRefreshToken("user01")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.reissue("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
