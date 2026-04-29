package com.sparta.delivhub.domain.area.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.util.AuthorizationUtils;
import com.sparta.delivhub.domain.area.dto.requset.AreaRequestDto;
import com.sparta.delivhub.domain.area.dto.response.AreaCityResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaNameResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaResponseDto;
import com.sparta.delivhub.domain.area.entity.Area;
import com.sparta.delivhub.domain.area.repository.AreaRepository;
import com.sparta.delivhub.domain.category.dto.requset.CategoryRequestDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryIdResponseDto;
import com.sparta.delivhub.domain.category.dto.response.CategoryNameResponseDto;
import com.sparta.delivhub.domain.category.entity.Category;
import com.sparta.delivhub.domain.category.repository.CategoryRepository;
import com.sparta.delivhub.domain.store.dto.requset.StoreRequestDto;
import com.sparta.delivhub.domain.store.dto.response.StoreDetailResponseDto;
import com.sparta.delivhub.domain.store.dto.response.StoreListResponseDto;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final UserRepository userRepository;

    @Transactional
    public AreaResponseDto createArea(AreaRequestDto areaRequestDto, String userId) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkAdminPermission(user);

        Area area = Area.builder()
                .city(areaRequestDto.getCity())
                .district(areaRequestDto.getDistrict())
                .name(areaRequestDto.getName())
                .build();

        Area save = areaRepository.save(area);

        return AreaResponseDto.builder()
                .city(save.getCity())
                .district(save.getDistrict())
                .name(save.getName())
                .build();
    }

    public List<AreaResponseDto> findAllAreas(Pageable pageable) {
        return areaRepository.findAll(pageable).stream()
                .map(AreaResponseDto::new)
                .toList();
    }

    public AreaCityResponseDto findArea(UUID id) {
        return areaRepository.findById(id)
                .map(AreaCityResponseDto::new)
                .orElseThrow(() -> new BusinessException(ErrorCode.AREA_NOT_FOUND_ON_READ));
    }

    @Transactional
    public AreaNameResponseDto updateArea(UUID id, AreaRequestDto areaRequestDto, String userId) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkAdminPermission(user);

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AREA_NOT_FOUND_ON_READ));

        area.updateArea(
                areaRequestDto.getName(),
                areaRequestDto.getCity(),
                areaRequestDto.getDistrict(),
                areaRequestDto.getIsHidden()
        );

        return AreaNameResponseDto.builder()
                .name(area.getName())
                .build();
    }

    @Transactional
    public AreaNameResponseDto deleteArea(UUID id, String userId) {

        User user = userRepository.findByUsernameAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkAdminPermission(user);

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AREA_NOT_FOUND_ON_READ));

        area.softDelete(userId);

        return AreaNameResponseDto.builder()
                .name(area.getName())
                .build();
    }

}
