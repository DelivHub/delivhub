package com.sparta.delivhub.domain.user.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.user.dto.UpdatePasswordRequest;
import com.sparta.delivhub.domain.user.dto.UpdateRoleRequest;
import com.sparta.delivhub.domain.user.dto.UpdateUserRequest;
import com.sparta.delivhub.domain.user.dto.UserResponse;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedOldPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();
    }

    @Test
    @DisplayName("유저_목록_조회_성공")
    void getUsers_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username").ascending());
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        given(userRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(userPage);

        // when
        PageResponse<UserResponse> response = userService.getUsers(null, null, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUsername()).isEqualTo("user01");
        assertThat(response.getContent().get(0).getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("유저_목록_조회_키워드_검색_성공")
    void getUsers_success_withKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        given(userRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(userPage);

        // when
        PageResponse<UserResponse> response = userService.getUsers("user01", null, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUsername()).isEqualTo("user01");
    }

    @Test
    @DisplayName("유저_목록_조회_역할_필터링_성공")
    void getUsers_success_withRole() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        given(userRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(userPage);

        // when
        PageResponse<UserResponse> response = userService.getUsers(null, "CUSTOMER", pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("유저_단건_조회_성공")
    void getUser_success() {
        // given
        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser("user01");

        // then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getEmail()).isEqualTo("user01@example.com");
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("유저_단건_조회_실패_존재X")
    void getUser_fail_notFound() {
        // given
        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser("user01"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저_닉네임_수정_성공")
    void updateUser_success_nickname() {
        // given
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        given(request.getNickname()).willReturn("새닉네임");
        given(request.getEmail()).willReturn(null);
        given(request.getIsPublic()).willReturn(null);

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateUser("user01", request);

        // then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("유저_이메일_수정_성공")
    void updateUser_success_email() {
        // given
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        given(request.getNickname()).willReturn(null);
        given(request.getEmail()).willReturn("newemail@example.com");
        given(request.getIsPublic()).willReturn(null);

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(userRepository.existsByEmail("newemail@example.com")).willReturn(false);

        // when
        UserResponse response = userService.updateUser("user01", request);

        // then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    @DisplayName("유저_공개여부_수정_성공")
    void updateUser_success_isPublic() {
        // given
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        given(request.getNickname()).willReturn(null);
        given(request.getEmail()).willReturn(null);
        given(request.getIsPublic()).willReturn(false);

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateUser("user01", request);

        // then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getIsPublic()).isFalse();
    }

    @Test
    @DisplayName("유저_정보_수정_실패_변경사항_없음")
    void updateUser_fail_noChanges() {
        // given
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        given(request.getNickname()).willReturn("홍길동");
        given(request.getEmail()).willReturn("user01@example.com");
        given(request.getIsPublic()).willReturn(true);

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.updateUser("user01", request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NO_CHANGES_DETECTED);
    }

    @Test
    @DisplayName("유저_정보_수정_실패_중복_이메일")
    void updateUser_fail_duplicateEmail() {
        // given
        UpdateUserRequest request = mock(UpdateUserRequest.class);
        given(request.getNickname()).willReturn(null);
        given(request.getEmail()).willReturn("duplicate@example.com");

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateUser("user01", request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("유저_역할_수정_성공")
    void updateRole_success() {
        // given
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        given(request.getRole()).willReturn(UserRole.OWNER);

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateRole("user01", request);

        // then
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getRole()).isEqualTo(UserRole.OWNER);
    }

    @Test
    @DisplayName("비밀번호_변경_성공")
    void updatePassword_success() {
        // given
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        given(request.getCurrentPassword()).willReturn("OldPassword1!");
        given(request.getNewPassword()).willReturn("NewPassword1!");

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("OldPassword1!", "encodedOldPassword")).willReturn(true);
        given(passwordEncoder.matches("NewPassword1!", "encodedOldPassword")).willReturn(false);
        given(passwordEncoder.encode("NewPassword1!")).willReturn("encodedNewPassword");

        // when
        userService.updatePassword("user01", request);

        // then
        verify(passwordEncoder).encode("NewPassword1!");
    }

    @Test
    @DisplayName("비밀번호_변경_실패_현재_비밀번호_불일치")
    void updatePassword_fail_wrongCurrentPassword() {
        // given
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        given(request.getCurrentPassword()).willReturn("WrongPassword1!");

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("WrongPassword1!", "encodedOldPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword("user01", request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.WRONG_CURRENT_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호_변경_실패_현재_비밀번호와_동일")
    void updatePassword_fail_sameAsCurrentPassword() {
        // given
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        given(request.getCurrentPassword()).willReturn("OldPassword1!");
        given(request.getNewPassword()).willReturn("OldPassword1!");

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("OldPassword1!", "encodedOldPassword")).willReturn(true);
        given(passwordEncoder.matches("OldPassword1!", "encodedOldPassword")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword("user01", request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_SAME_AS_CURRENT);
    }

    @Test
    @DisplayName("유저_삭제_성공")
    void deleteUser_success() {
        // given
        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        userService.deleteUser("user01", "admin");

        // then
        verify(userRepository).findByUsernameAndDeletedAtIsNull("user01");
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("유저_삭제_실패_존재X")
    void deleteUser_fail_notFound() {
        // given
        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser("user01", "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
