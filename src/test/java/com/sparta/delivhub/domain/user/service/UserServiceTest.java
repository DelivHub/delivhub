package com.sparta.delivhub.domain.user.service;

import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.user.dto.UpdatePasswordRequest;
import com.sparta.delivhub.domain.user.dto.UpdateRoleRequest;
import com.sparta.delivhub.domain.user.dto.UpdateUserRequest;
import com.sparta.delivhub.domain.user.dto.UserResponse;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
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

    @Test
    @DisplayName("유저_목록_조회_성공")
    void getUsers_success() {
        // given
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
    @DisplayName("유저_닉네임_수정_성공")
    void updateUser_success_nickname() {
        // given
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
    @DisplayName("유저_역할_수정_성공")
    void updateRole_success() {
        // given
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedOldPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

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
    @DisplayName("유저_삭제_성공")
    void deleteUser_success() {
        // given
        User user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

        given(userRepository.findByUsernameAndDeletedAtIsNull("user01")).willReturn(Optional.of(user));

        // when
        userService.deleteUser("user01", "admin");

        // then
        verify(userRepository).findByUsernameAndDeletedAtIsNull("user01");
        assertThat(user.getDeletedAt()).isNotNull();
    }
}
