package com.sparta.delivhub.domain.user.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.payment.dto.MyPaymentListResponseDto;
import com.sparta.delivhub.domain.payment.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.user.dto.UpdatePasswordRequest;
import com.sparta.delivhub.domain.user.dto.UpdateRoleRequest;
import com.sparta.delivhub.domain.user.dto.UpdateUserRequest;
import com.sparta.delivhub.domain.user.dto.UserResponse;
import com.sparta.delivhub.domain.user.service.UserService;
import com.sparta.delivhub.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.PushbackReader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final PaymentService paymentService;
    private final UserService userService;

    /**
     * 내 결제 내역 전체 조회
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<MyPaymentListResponseDto>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 시큐리티 토큰에서 로그인한 유저 정보 추출
        String currentUserId = userDetails.getUsername();

        // 2. 서비스 로직 호출
        MyPaymentListResponseDto responseData = paymentService.getMyPayments(currentUserId, pageable);

        // 3. 성공 응답 반환 (200 OK)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<UserResponse> response = userService.getUsers(keyword, role, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("#username == authentication.name or hasAnyRole('MANAGER', 'MASTER')")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable String username
    ) {
        UserResponse response = userService.getUser(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("#username == authentication.name or hasAnyRole('MANAGER', 'MASTER')")
    @PutMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserInfo(
            @PathVariable String username,
            @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateUserInfo(username, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('MASTER') and #username != authentication.name")
    @PutMapping("/{username}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String username,
            @RequestBody UpdateRoleRequest request
    ) {
        UserResponse response = userService.updateUserRole(username, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("#username == authentication.name")
    @PutMapping("/{username}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable String username,
            @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(username, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER') and #username != authentication.name")
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.deleteUser(username, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }
}
