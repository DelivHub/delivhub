package com.sparta.delivhub.domain.address.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.dto.PageResponse;
import com.sparta.delivhub.domain.address.dto.AddressResponse;
import com.sparta.delivhub.domain.address.dto.AddressRequest;
import com.sparta.delivhub.domain.address.dto.SetDefaultRequest;
import com.sparta.delivhub.domain.address.dto.UpdateAddressRequest;
import com.sparta.delivhub.domain.address.entity.Address;
import com.sparta.delivhub.domain.address.repository.AddressRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Transactional
    public AddressResponse createAddress(String username, AddressRequest request) {
        User user = userService.findUserByUsername(username);

        Address address = Address.builder()
                .user(user)
                .alias(request.getAlias())
                .address(request.getAddress())
                .detail(request.getDetail())
                .zipCode(request.getZipCode())
                .build();

        return AddressResponse.from(addressRepository.save(address));
    }

    public PageResponse<AddressResponse> getAddresses(
            String username,
            String keyword,
            Pageable pageable
    ) {
        User user = userService.findUserByUsername(username);

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "isDefault")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable validatedPageable = PageRequest.of(
                pageable.getPageNumber(),
                size,
                sort
        );

        Specification<Address> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("user"), user),
                        cb.isNull(root.get("deletedAt"))
                );

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("alias"), "%" + keyword + "%"),
                            cb.like(root.get("address"), "%" + keyword + "%")
                    )
            );
        }

        Page<Address> addressPage = addressRepository.findAll(spec, validatedPageable);
        return new PageResponse<>(addressPage.map(AddressResponse::from));
    }

    public AddressResponse getAddress(String username, UUID addressId) {
        Address address = findAddressById(addressId);
        validateOwner(address, username);
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse updateAddress(String username, UUID addressId, UpdateAddressRequest request) {
        Address address = findAddressById(addressId);
        validateOwner(address, username);

        boolean aliasUnchanged = request.getAlias() == null
                || request.getAlias().equals(address.getAlias());

        boolean addressUnchanged = request.getAddress() == null
                || request.getAddress().equals(address.getAddress());

        boolean detailUnchanged = request.getDetail() == null
                || request.getDetail().equals(address.getDetail());

        boolean zipCodeUnchanged = request.getZipCode() == null
                || request.getZipCode().equals(address.getZipCode());

        if (aliasUnchanged && addressUnchanged && detailUnchanged && zipCodeUnchanged) {
            throw new BusinessException(ErrorCode.NO_CHANGES_DETECTED);
        }

        address.updateAddress(request.getAlias(), request.getAddress(), request.getDetail(), request.getZipCode());
        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(UUID addressId, String username, UserRole requesterRole) {
        Address address = findAddressById(addressId);

        if (address.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_ADDRESS);
        }

        if (requesterRole != UserRole.MASTER) {
            validateOwner(address, username);
        }

        address.softDelete(username);
    }

    @Transactional
    public AddressResponse setDefault(String username, UUID addressId, SetDefaultRequest request) {
        Address address = findAddressById(addressId);
        validateOwner(address, username);

        if (Boolean.TRUE.equals(request.getIsDefault())) {

            // 기존 기본 배송지 모두 해제
            List<Address> currentDefaults =
                    addressRepository.findByUserAndIsDefaultTrueAndDeletedAtIsNull(address.getUser());
            currentDefaults.forEach(a -> a.changeDefault(false));
        }

        address.changeDefault(request.getIsDefault());
        return AddressResponse.from(address);
    }

    public Address findAddressById(UUID addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
    }

    // 본인 확인
    private void validateOwner(Address address, String username) {
        if (!address.getUser().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
