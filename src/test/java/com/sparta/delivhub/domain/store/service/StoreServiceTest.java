package com.sparta.delivhub.domain.store.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.area.entity.Area;
import com.sparta.delivhub.domain.area.repository.AreaRepository;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.domain.category.repository.CategoryRepository;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreNameResponseDto;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("가게 생성 성공 - 사장님 권한으로 생성")
    void createStore_Success() {
        // given
        String userId = "ownerUser";
        UUID categoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        StoreRequestDto request = new StoreRequestDto("원주반점", categoryId, areaId, "원주시 무실동", "033-123-4567");

        User owner = User.builder()
                .username(userId)
                .userRole(UserRole.OWNER)
                .build();

        Category category = Category.builder().id(categoryId).name("중식").build();
        Area area = Area.builder().id(areaId).name("원주").build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(owner));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(areaRepository.findById(areaId)).willReturn(Optional.of(area));

        UUID savedStoreId = UUID.randomUUID();
        Store savedStore = Store.builder()
                .id(savedStoreId)
                .name(request.getName())
                .address(request.getAddress())
                .number(request.getNumber())
                .averageRating(BigDecimal.ZERO)
                .build();

        given(storeRepository.save(any(Store.class))).willReturn(savedStore);

        // when
        StoreDetailResponseDto response = storeService.createStore(request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStoreId()).isEqualTo(savedStoreId);
        assertThat(response.getName()).isEqualTo("원주반점");
        assertThat(response.getAverage_rating()).isEqualTo(BigDecimal.ZERO); // 평점 0.0 검증

        verify(userRepository, times(1)).findByUsernameAndDeletedAtIsNull(userId);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("가게 생성 실패")
    void createStore_Fail_NotOwner() {
        // given
        String userId = "customerUser";
        StoreRequestDto request = new StoreRequestDto("가게명", UUID.randomUUID(), UUID.randomUUID(), "주소", "번호");
        User customer = User.builder().username(userId).userRole(UserRole.CUSTOMER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(customer));

        // when & then
        assertThatThrownBy(() -> storeService.createStore(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("가게 전체 목록 조회")
    void findAllStores_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Store store = Store.builder().name("원주반점").averageRating(BigDecimal.ZERO).build();
        given(storeRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(store)));

        // when
        List<StoreListResponseDto> result = storeService.findAllStores(pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("원주반점");
        verify(storeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    void findStore_Success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder()
                .id(storeId)
                .name("원주반점")
                .address("원주시 무실동")
                .averageRating(BigDecimal.ZERO)
                .build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when
        StoreDetailResponseDto response = storeService.findStore(storeId);

        // then
        assertThat(response.getName()).isEqualTo("원주반점");
        assertThat(response.getAddress()).isEqualTo("원주시 무실동");
        verify(storeRepository).findById(storeId);
    }

    @Test
    @DisplayName("가게 수정 성공")
    void updateStore_Success() {
        // given
        UUID storeId = UUID.randomUUID();
        String userId = "ownerUser";
        StoreRequestDto updateRequest = new StoreRequestDto("수정된 가게", UUID.randomUUID(), UUID.randomUUID(), "주소", "010-0000");

        User owner = User.builder().username(userId).userRole(UserRole.OWNER).build();
        Store store = Store.builder().id(storeId).owner(owner).name("기존 가게").build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(categoryRepository.findById(any())).willReturn(Optional.of(Category.builder().build()));
        given(areaRepository.findById(any())).willReturn(Optional.of(Area.builder().build()));
        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(owner));

        // when
        StoreNameResponseDto response = storeService.updateStore(storeId, updateRequest, userId);

        // then
        assertThat(store.getName()).isEqualTo("수정된 가게");
        assertThat(response.getName()).isEqualTo("수정된 가게");
    }

    @Test
    @DisplayName("가게 삭제 성공")
    void deleteStore_Success() {
        // given
        UUID storeId = UUID.randomUUID();
        String userId = "ownerUser";
        User owner = User.builder().username(userId).userRole(UserRole.OWNER).build();
        Store store = Store.builder().id(storeId).owner(owner).build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(owner));

        // when
        storeService.deleteStore(storeId, userId);

        // then
        assertThat(store.getDeletedAt()).isNotNull();
        assertThat(store.getDeletedBy()).isEqualTo(userId);
    }
}