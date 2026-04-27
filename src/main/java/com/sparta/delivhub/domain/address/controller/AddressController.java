package com.sparta.delivhub.domain.address.controller;

import com.sparta.delivhub.common.dto.ApiResponse;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.address.dto.AddressRequest;
import com.sparta.delivhub.domain.address.dto.AddressResponse;
import com.sparta.delivhub.domain.address.dto.SetDefaultRequest;
import com.sparta.delivhub.domain.address.dto.UpdateAddressRequest;
import com.sparta.delivhub.domain.address.service.AddressService;
import com.sparta.delivhub.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.createAddress(userDetails.getUsername(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AddressResponse> response =
                addressService.getAddresses(userDetails.getUsername(), keyword, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID addressId
    ) {
        AddressResponse response = addressService.getAddress(userDetails.getUsername(), addressId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        AddressResponse response =
                addressService.updateAddress(userDetails.getUsername(), addressId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID addressId
    ) {
        addressService.deleteAddress(addressId, userDetails.getUsername(), userDetails.getUserRole());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID addressId,
            @Valid @RequestBody SetDefaultRequest request
    ) {
        AddressResponse response =
                addressService.setDefault(userDetails.getUsername(), addressId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
