package com.sparta.delivhub.domain.area.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.area.dto.requset.AreaRequestDto;
import com.sparta.delivhub.domain.area.dto.response.AreaCityResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaIdResponseDto;
import com.sparta.delivhub.domain.area.dto.response.AreaResponseDto;
import com.sparta.delivhub.domain.area.entity.Area;
import com.sparta.delivhub.domain.area.repository.AreaRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @InjectMocks
    private AreaService areaService;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("지역 생성 성공")
    void createArea_Success() {
        // given
        String userId = "manager01";
        AreaRequestDto request = new AreaRequestDto("강원도", "원주시", "무실동");
        User manager = User.builder().username(userId).userRole(UserRole.MANAGER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(manager));
        Area savedArea = Area.builder().id(UUID.randomUUID()).city("강원도").district("원주시").name("무실동").build();
        given(areaRepository.save(any(Area.class))).willReturn(savedArea);

        // when
        AreaIdResponseDto response = areaService.createArea(request, userId);

        // then
        assertThat(response.getAreaId()).isEqualTo(savedArea.getId());
        verify(areaRepository).save(any(Area.class));
    }

    @Test
    @DisplayName("지역 생성 실패")
    void createArea_Fail_Forbidden() {
        // given
        String userId = "owner01";
        AreaRequestDto request = new AreaRequestDto("강원도", "원주시", "단계동");
        User owner = User.builder().username(userId).userRole(UserRole.OWNER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(owner));

        // when & then
        assertThatThrownBy(() -> areaService.createArea(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }


    @Test
    @DisplayName("지역 목록 조회")
    void findAllAreas_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Area area = Area.builder().city("서울특별시").district("종로구").name("광화문").build();
        Page<Area> areaPage = new PageImpl<>(List.of(area));

        given(areaRepository.findAll(pageable)).willReturn(areaPage);

        // when
        List<AreaResponseDto> result = areaService.findAllAreas(pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("서울특별시");
        verify(areaRepository).findAll(pageable);
    }

    @Test
    @DisplayName("지역 상세 조회 성공")
    void findArea_Success() {
        // given
        UUID areaId = UUID.randomUUID();
        Area area = Area.builder()
                .id(areaId)
                .city("강원도")
                .district("원주시")
                .name("무실동")
                .build();
        given(areaRepository.findById(areaId)).willReturn(Optional.of(area));

        // when
        AreaCityResponseDto response = areaService.findArea(areaId);

        // then
        assertThat(response.getCity()).isEqualTo("강원도");
        verify(areaRepository).findById(areaId);
    }

    @Test
    @DisplayName("지역 삭제 성공")
    void deleteArea_Success() {
        // given
        UUID areaId = UUID.randomUUID();
        String userId = "master01";
        User master = User.builder().username(userId).userRole(UserRole.MASTER).build();
        Area area = Area.builder().id(areaId).city("강원도").district("원주시").name("흥양리").build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(userId)).willReturn(Optional.of(master));
        given(areaRepository.findById(areaId)).willReturn(Optional.of(area));

        // when
        areaService.deleteArea(areaId, userId);

        // then
        assertThat(area.getDeletedAt()).isNotNull();
        assertThat(area.getDeletedBy()).isEqualTo(userId);
    }
}