package com.sparta.delivhub.domain.menu.service;

import com.sparta.delivhub.domain.ai.service.AiService;
import com.sparta.delivhub.domain.menu.dto.CreateMenuDto;
import com.sparta.delivhub.domain.menu.dto.HiddenMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuListDto;
import com.sparta.delivhub.domain.menu.dto.UpdateMenuDto;
import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AiService aiService;

    private UUID storeId;
    private UUID menuId;
    private Store store;
    private Menu menu;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        menuId = UUID.randomUUID();

        User mockOwner = mock(User.class);
        lenient().when(mockOwner.getUsername()).thenReturn("owner1");

        store = mock(Store.class);
        lenient().when(store.getId()).thenReturn(storeId);
        lenient().when(store.getOwner()).thenReturn(mockOwner);

        menu = mock(Menu.class);
        lenient().when(menu.getId()).thenReturn(menuId);
        lenient().when(menu.getStore()).thenReturn(store);
        lenient().when(menu.getName()).thenReturn("김치찌개");
        lenient().when(menu.getPrice()).thenReturn(9000);
        lenient().when(menu.getDescription()).thenReturn("맛있는 김치찌개");
        lenient().when(menu.isHidden()).thenReturn(false);
        lenient().when(menu.getCreatedAt()).thenReturn(LocalDateTime.now());
        lenient().when(menu.getCreatedBy()).thenReturn("owner1");

        User mockUser = mock(User.class);
        lenient().when(mockUser.getUserRole()).thenReturn(UserRole.MASTER);
        lenient().when(userRepository.findByUsernameAndDeletedAtIsNull(any())).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("메뉴 등록 성공")
    void createMenu() {
        // given
        CreateMenuDto request = new CreateMenuDto();
        ReflectionTestUtils.setField(request, "name", "김치찌개");
        ReflectionTestUtils.setField(request, "price", 9000);
        
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        // when
        ResponseMenuDto response = menuService.createMenu(storeId, request, "admin");

        // then
        assertThat(response).isNotNull();
        verify(menuRepository, times(1)).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 성공 - AI 설명 자동 생성")
    void createMenu_WithAiDescription() {
        // given
        CreateMenuDto request = new CreateMenuDto();
        ReflectionTestUtils.setField(request, "name", "제육볶음");
        ReflectionTestUtils.setField(request, "price", 10000);
        ReflectionTestUtils.setField(request, "aiDescription", true);
        ReflectionTestUtils.setField(request, "aiPrompt", "매콤한 제육볶음 설명해줘");

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(aiService.generateDescription(anyString(), anyString())).thenReturn("AI가 생성한 설명");
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        // when
        menuService.createMenu(storeId, request, "owner1");

        // then
        verify(aiService, times(1)).generateDescription(anyString(), eq("매콤한 제육볶음 설명해줘"));
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 권한 없는 유저")
    void createMenu_Fail_Unauthorized() {
        // given
        CreateMenuDto request = new CreateMenuDto();
        User hacker = mock(User.class);
        lenient().when(hacker.getUsername()).thenReturn("hacker");
        lenient().when(hacker.getUserRole()).thenReturn(UserRole.CUSTOMER);
        
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(userRepository.findByUsernameAndDeletedAtIsNull("hacker")).thenReturn(Optional.of(hacker));

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(storeId, request, "hacker"))
                .isInstanceOf(com.sparta.delivhub.common.dto.BusinessException.class);
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공")
    void getMenus() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Menu> menuPage = new PageImpl<>(List.of(menu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.findByStoreIdAndIsHiddenFalseAndDeletedAtIsNull(storeId, pageable)).thenReturn(menuPage);

        // when
        Page<ResponseMenuListDto> response = menuService.getMenus(storeId, pageable, false);

        // then
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("메뉴 상세 조회 성공")
    void getMenu() {
        // given
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findAllByMenuIdWithItems(menuId)).thenReturn(List.of());

        // when
        ResponseMenuDto response = menuService.getMenu(menuId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void updateMenu() {
        // given
        UpdateMenuDto request = new UpdateMenuDto();
        ReflectionTestUtils.setField(request, "name", "수정된 김치찌개");
        
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findAllByMenuIdWithItems(menuId)).thenReturn(List.of());

        // when
        menuService.updateMenu(menuId, request, "admin");

        // then
        verify(menu, times(1)).update(eq("수정된 김치찌개"), any(), any());
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu() {
        // given
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

        // when
        menuService.deleteMenu(menuId, "owner1");

        // then
        verify(menu, times(1)).softDelete("owner1");
    }
}
