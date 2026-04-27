package com.sparta.delivhub.domain.option.service;

import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import com.sparta.delivhub.domain.option.dto.UpdateOptionDto;
import com.sparta.delivhub.domain.option.entity.Option;
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {
    @InjectMocks
    private OptionService optionService;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserRepository userRepository;

    private UUID menuId;
    private UUID optionId;
    private Menu menu;
    private Option option;

    @BeforeEach
    void setUp() {
        menuId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        User mockOwner = mock(User.class);
        lenient().when(mockOwner.getUsername()).thenReturn("owner1");

        Store mockStore = mock(Store.class);
        lenient().when(mockStore.getOwner()).thenReturn(mockOwner);

        menu = mock(Menu.class);
        lenient().when(menu.getId()).thenReturn(menuId);
        lenient().when(menu.getStore()).thenReturn(mockStore);

        option = mock(Option.class);
        lenient().when(option.getId()).thenReturn(optionId);
        lenient().when(option.getName()).thenReturn("소스 추가");
        lenient().when(option.getExtraPrice()).thenReturn(500L);
        lenient().when(option.getMenu()).thenReturn(menu);

        User mockUser = mock(User.class);
        lenient().when(mockUser.getUserRole()).thenReturn(UserRole.MASTER);
        lenient().when(userRepository.findByUsernameAndDeletedAtIsNull(any())).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("옵션 등록 성공")
    void createOption() {
        // given
        CreateOptionDto request = new CreateOptionDto("소스 추가", 500L);
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.save(any(Option.class))).thenReturn(option);

        // when
        ResponseOptionDto response = optionService.createOption(menuId, request, null);

        // then
        assertThat(response).isNotNull();
        verify(optionRepository, times(1)).save(any(Option.class));
    }

    @Test
    @DisplayName("옵션 목록 조회 성공")
    void getOptions() {
        // given
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findByMenuIdAndDeletedAtIsNull(menuId)).thenReturn(List.of(option));

        // when
        List<ResponseOptionDto> response = optionService.getOptions(menuId);

        // then
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("옵션 수정 성공")
    void updateOption() {
        // given
        UpdateOptionDto request = new UpdateOptionDto("수정된 옵션명", 1000L);
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findByIdAndDeletedAtIsNull(optionId)).thenReturn(Optional.of(option));

        // when
        ResponseOptionDto response = optionService.updateOption(menuId, optionId, request, null);

        // then
        assertThat(response).isNotNull();
        verify(option, times(1)).update(request.getName(), request.getExtraPrice());
    }

    @Test
    @DisplayName("옵션 삭제 성공")
    void deleteOption() {
        // given
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findByIdAndDeletedAtIsNull(optionId)).thenReturn(Optional.of(option));

        // when
        optionService.deleteOption(menuId, optionId, null);

        // then
        verify(option, times(1)).softDelete(null);
    }
}