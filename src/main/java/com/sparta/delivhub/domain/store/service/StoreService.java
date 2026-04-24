package com.sparta.delivhub.domain.store.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.area.entity.Area;
import com.sparta.delivhub.domain.area.repository.AreaRepository;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.domain.category.repository.CategoryRepository;
import com.sparta.delivhub.domain.review.repository.ReviewRepository;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreIdResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final AreaRepository areaRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public StoreIdResponseDto createStore(StoreRequestDto storeRequestDto) {
        Category category = categoryRepository.findById(storeRequestDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Area area = areaRepository.findById(storeRequestDto.getAreaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AREA_NOT_FOUND_ON_READ));

        Store store = Store.builder()
                .name(storeRequestDto.getName())
                .category(category)
                .area(area)
                .address(storeRequestDto.getAddress())
                .number(storeRequestDto.getNumber())
                .isHidden(storeRequestDto.getIsHidden())
                .build();
        storeRepository.save(store);


        return StoreIdResponseDto.builder()
                .storeId(store.getId())
                .build();
    }

    public List<StoreListResponseDto> findAllStores() {
        return storeRepository.findAll().stream()
                .map(StoreListResponseDto::new)
                .toList();
    }

    public StoreDetailResponseDto findStore(UUID id) {
        return storeRepository.findById(id)
                .map(StoreDetailResponseDto::new)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }
    @Transactional
    public StoreIdResponseDto updateStore(UUID id, StoreRequestDto storeRequestDto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        Category category = categoryRepository.findById(storeRequestDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        Area area = areaRepository.findById(storeRequestDto.getAreaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AREA_NOT_FOUND_ON_READ));

        store.updateStore(
                storeRequestDto.getName(),
                storeRequestDto.getAddress(),
                storeRequestDto.getIsHidden(),
                category,
                area,
                storeRequestDto.getNumber()
        );

        return StoreIdResponseDto.builder()
                .storeId(store.getId())
                .build();
    }

    @Transactional
    public StoreIdResponseDto deleteStore(UUID id, String userId) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        store.softDelete(userId);

        return StoreIdResponseDto.builder()
                .storeId(store.getId())
                .build();
    }

}
