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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UserResponse> getUsers(
            String keyword,
            String role,
            Pageable pageable
    ) {

        int size = pageable.getPageSize();

        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        Pageable validatedPageable = PageRequest.of(
                pageable.getPageNumber(),
                size,
                pageable.getSort()
        );

        Specification<User> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("username"), "%" + keyword + "%"),
                            cb.like(root.get("nickname"), "%" + keyword + "%")
                    )
            );
        }

        if (role != null && !role.isBlank()) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userRole"), userRole));
        }

        Page<User> userPage = userRepository.findAll(spec, validatedPageable);

        return new PageResponse<>(userPage.map(UserResponse::from));
    }

    public UserResponse getUser(String username) {
        User user = findUserByUsername(username);
        return UserResponse.from(user);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserResponse updateUserInfo(String username, UpdateUserRequest request) {
        User user = findUserByUsername(username);

        boolean nicknameUnchanged = request.getNickname() == null
                || request.getNickname().equals(user.getNickname());

        // 중복 이메일 검증
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        boolean emailUnchanged = request.getEmail() == null
                || request.getEmail().equals(user.getEmail());

        boolean isPublicUnchanged = request.getIsPublic() == null
                || request.getIsPublic().equals(user.getIsPublic());

        if (nicknameUnchanged && emailUnchanged && isPublicUnchanged) {
            throw new BusinessException(ErrorCode.NO_CHANGES_DETECTED);
        }

        user.updateUser(request.getNickname(), request.getEmail(), request.getIsPublic());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUserRole(String username, UpdateRoleRequest request) {
        User user = findUserByUsername(username);
        user.updateRole(request.getRole());
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = findUserByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_CURRENT);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // todo : 토큰 무효화
    }

    @Transactional
    public void deleteUser(String username, String deletedBy) {
        User user = findUserByUsername(username);
        user.softDelete(deletedBy);

        // todo : 토큰 무효화
    }
}
