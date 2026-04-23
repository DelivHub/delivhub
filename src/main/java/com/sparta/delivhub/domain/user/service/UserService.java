package com.sparta.delivhub.domain.user.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.user.dto.UserResponse;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("#username == authentication.name or hasAnyRole('MANAGER', 'MASTER')")
    public UserResponse getUser(String username) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
