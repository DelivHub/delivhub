package com.sparta.delivhub.domain.auth.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.domain.auth.dto.*;
import com.sparta.delivhub.domain.auth.service.AuthService;
import com.sparta.delivhub.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<?>> reissue(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String refreshToken = bearerToken.substring(7);
        ReissueResponse response = authService.reissue(refreshToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

}
