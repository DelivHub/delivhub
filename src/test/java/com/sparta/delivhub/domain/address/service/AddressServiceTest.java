package com.sparta.delivhub.domain.address.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address address;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user01")
                .email("user01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("홍길동")
                .build();

        addressId = UUID.randomUUID();

        address = Address.builder()
                .user(user)
                .alias("집")
                .address("서울시 종로구 세종대로 172")
                .detail("101동 1001호")
                .zipCode("03154")
                .build();
    }

    @Test
    @DisplayName("배송지_등록_성공")
    void createAddress_success() {
        // given
        AddressRequest request = mock(AddressRequest.class);
        given(request.getAlias()).willReturn("집");
        given(request.getAddress()).willReturn("서울시 종로구 세종대로 172");
        given(request.getDetail()).willReturn("101동 1001호");
        given(request.getZipCode()).willReturn("03154");

        given(userService.findUserByUsername("user01")).willReturn(user);
        given(addressRepository.save(any(Address.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        AddressResponse response = addressService.createAddress("user01", request);

        // then
        assertThat(response.getUserId()).isEqualTo("user01");
        assertThat(response.getAlias()).isEqualTo("집");
        assertThat(response.getAddress()).isEqualTo("서울시 종로구 세종대로 172");
        assertThat(response.getDetail()).isEqualTo("101동 1001호");
        assertThat(response.getZipCode()).isEqualTo("03154");
        assertThat(response.getIsDefault()).isFalse();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("배송지_목록_조회_성공")
    void getAddresses_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> addressPage = new PageImpl<>(List.of(address), pageable, 1);

        given(userService.findUserByUsername("user01")).willReturn(user);
        given(addressRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(addressPage);

        // when
        PageResponse<AddressResponse> response = addressService.getAddresses("user01", null, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAlias()).isEqualTo("집");
        assertThat(response.getContent().get(0).getUserId()).isEqualTo("user01");
    }

    @Test
    @DisplayName("배송지_목록_조회_키워드_검색_성공")
    void getAddresses_success_withKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> addressPage = new PageImpl<>(List.of(address), pageable, 1);

        given(userService.findUserByUsername("user01")).willReturn(user);
        given(addressRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(addressPage);

        // when
        PageResponse<AddressResponse> response = addressService.getAddresses("user01", "집", pageable);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAlias()).isEqualTo("집");
    }

    @Test
    @DisplayName("배송지_상세_조회_성공")
    void getAddress_success() {
        // given
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        AddressResponse response = addressService.getAddress("user01", addressId);

        // then
        assertThat(response.getAlias()).isEqualTo("집");
        assertThat(response.getAddress()).isEqualTo("서울시 종로구 세종대로 172");
        assertThat(response.getUserId()).isEqualTo("user01");
    }

    @Test
    @DisplayName("배송지_수정_성공")
    void updateAddress_success() {
        // given
        UpdateAddressRequest request = mock(UpdateAddressRequest.class);
        given(request.getAlias()).willReturn("자취방");        // 별칭 변경
        given(request.getAddress()).willReturn("서울시 종로구 세종대로 172");
        given(request.getDetail()).willReturn("101동 1001호");
        given(request.getZipCode()).willReturn("03154");

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        AddressResponse response = addressService.updateAddress("user01", addressId, request);

        // then
        assertThat(response.getAlias()).isEqualTo("자취방");
        assertThat(response.getUserId()).isEqualTo("user01");
    }

    @Test
    @DisplayName("배송지_삭제_성공_본인")
    void deleteAddress_success_owner() {
        // given
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        addressService.deleteAddress(addressId, "user01", UserRole.CUSTOMER);

        // then
        verify(addressRepository).findById(addressId);
        assertThat(address.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("배송지_삭제_성공_MASTER")
    void deleteAddress_success_master() {
        // given
        User otherUser = User.builder()
                .username("other01")
                .email("other01@example.com")
                .password("encodedPassword")
                .userRole(UserRole.CUSTOMER)
                .nickname("다른유저")
                .build();

        Address otherAddress = Address.builder()
                .user(otherUser)
                .alias("집")
                .address("서울시 강남구 테헤란로 1")
                .detail("101호")
                .zipCode("06142")
                .build();

        given(addressRepository.findById(addressId)).willReturn(Optional.of(otherAddress));

        // when
        addressService.deleteAddress(addressId, "master01", UserRole.MASTER);

        // then
        assertThat(otherAddress.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("기본_배송지_설정_성공_true")
    void setDefault_true_success() {
        // given
        Address existingDefault = Address.builder()
                .user(user)
                .alias("회사")
                .address("서울시 강남구 테헤란로 1")
                .detail("10층")
                .zipCode("06142")
                .isDefault(true)
                .build();

        SetDefaultRequest request = mock(SetDefaultRequest.class);
        given(request.getIsDefault()).willReturn(true);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
        given(addressRepository.findByUserAndIsDefaultTrueAndDeletedAtIsNull(user))
                .willReturn(List.of(existingDefault));

        // when
        AddressResponse response = addressService.setDefault("user01", addressId, request);

        // then
        assertThat(existingDefault.getIsDefault()).isFalse();
        assertThat(response.getIsDefault()).isTrue();
        assertThat(response.getUserId()).isEqualTo("user01");
    }

    @Test
    @DisplayName("기본_배송지_설정_취소_성공_false")
    void setDefault_false_success() {
        // given
        Address defaultAddress = Address.builder()
                .user(user)
                .alias("집")
                .address("서울시 종로구 세종대로 172")
                .detail("101동 1001호")
                .zipCode("03154")
                .isDefault(true)
                .build();

        SetDefaultRequest request = mock(SetDefaultRequest.class);
        given(request.getIsDefault()).willReturn(false);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(defaultAddress));

        // when
        AddressResponse response = addressService.setDefault("user01", addressId, request);

        // then
        assertThat(response.getIsDefault()).isFalse();
        verify(addressRepository, never())  // false 설정 시 기존 기본 배송지 초기화 불필요
                .findByUserAndIsDefaultTrueAndDeletedAtIsNull(any());
    }
}
