package com.sparta.delivhub.domain.menu.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.ai.service.AiService;
import com.sparta.delivhub.domain.menu.dto.CreateMenuDto;
import com.sparta.delivhub.domain.menu.dto.HiddenMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuListDto;
import com.sparta.delivhub.domain.menu.dto.UpdateMenuDto;
import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import com.sparta.delivhub.domain.option.entity.OptionType;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private User admin;

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

        admin = mock(User.class);
        lenient().when(admin.getUsername()).thenReturn("admin");
        lenient().when(admin.getUserRole()).thenReturn(UserRole.MASTER);
        lenient().when(userRepository.findByUsernameAndDeletedAtIsNull(any())).thenReturn(Optional.of(admin));
    }

    @Test
    @DisplayName("메뉴 등록 성공")
    void createMenu() {
        CreateMenuDto request = new CreateMenuDto();
        ReflectionTestUtils.setField(request, "name", "김치찌개");
        ReflectionTestUtils.setField(request, "price", 9000);
        
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        ResponseMenuDto response = menuService.createMenu(storeId, request, "admin");

        assertThat(response).isNotNull();
        verify(menuRepository, times(1)).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 성공 - 옵션 포함")
    void createMenu_WithOptions() {
        CreateMenuDto request = new CreateMenuDto();
        ReflectionTestUtils.setField(request, "name", "치킨");
        ReflectionTestUtils.setField(request, "price", 20000);
        
        CreateOptionDto.Item item = new CreateOptionDto.Item("양념", 1000L);
        CreateOptionDto optionDto = new CreateOptionDto("소스선택", OptionType.SINGLE, List.of(item));
        
        ReflectionTestUtils.setField(request, "options", List.of(optionDto));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        menuService.createMenu(storeId, request, "admin");

        verify(optionRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("메뉴 등록 실패 - AI 프롬프트 누락")
    void createMenu_Fail_AiPromptRequired() {
        CreateMenuDto request = new CreateMenuDto();
        ReflectionTestUtils.setField(request, "aiDescription", true);
        ReflectionTestUtils.setField(request, "aiPrompt", "");

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> menuService.createMenu(storeId, request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.AI_PROMPT_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("메뉴 목록 조회 - 관리자 권한 (숨김 메뉴 포함)")
    void getMenus_Admin() {
        Pageable pageable = PageRequest.of(0, 10);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.findByStoreIdAndDeletedAtIsNull(eq(storeId), any())).thenReturn(new PageImpl<>(List.of(menu)));

        Page<ResponseMenuListDto> response = menuService.getMenus(storeId, pageable, true);

        assertThat(response.getContent()).hasSize(1);
        verify(menuRepository).findByStoreIdAndDeletedAtIsNull(any(), any());
    }

    @Test
    @DisplayName("메뉴 목록 조회 실패 - 가게 없음")
    void getMenus_Fail_StoreNotFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenus(storeId, PageRequest.of(0, 10), false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴 숨김 상태 수정 성공")
    void updateMenuHidden() {
        HiddenMenuDto request = new HiddenMenuDto();
        ReflectionTestUtils.setField(request, "isHidden", true);

        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

        menuService.updateMenuHidden(menuId, request, "admin");

        verify(menu).updateHidden(true);
    }

    @Test
    @DisplayName("이미지 기반 AI 설명 생성 성공")
    void generateDescriptionFromImage_Success() {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(aiService.generateDescriptionFromImage(anyString(), any(MultipartFile.class))).thenReturn("이미지 분석 결과");

        menuService.generateDescriptionFromImage(menuId, image, "admin");

        verify(aiService).generateDescriptionFromImage(eq("admin"), any());
        verify(menu).update(any(), any(), eq("이미지 분석 결과"));
    }

    @Test
    @DisplayName("이미지 기반 AI 설명 생성 실패 - 이미지 누락")
    void generateDescriptionFromImage_Fail_EmptyImage() {
        MockMultipartFile image = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

        assertThatThrownBy(() -> menuService.generateDescriptionFromImage(menuId, image, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.AI_IMAGE_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("메뉴 조회 실패 - 유저 없음")
    void getMenuAndCheckPermission_Fail_UserNotFound() {
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(userRepository.findByUsernameAndDeletedAtIsNull("none")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.deleteMenu(menuId, "none"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void updateMenu() {
        UpdateMenuDto request = new UpdateMenuDto();
        ReflectionTestUtils.setField(request, "name", "수정된 김치찌개");
        
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findAllByMenuIdWithItems(menuId)).thenReturn(List.of());

        menuService.updateMenu(menuId, request, "admin");

        verify(menu, times(1)).update(eq("수정된 김치찌개"), any(), any());
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu() {
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));

        menuService.deleteMenu(menuId, "owner1");

        verify(menu, times(1)).softDelete("owner1");
    }

    @Test
    @DisplayName("메뉴 상세 조회 성공")
    void getMenu() {
        when(menuRepository.findByIdAndDeletedAtIsNull(menuId)).thenReturn(Optional.of(menu));
        when(optionRepository.findAllByMenuIdWithItems(menuId)).thenReturn(List.of());

        ResponseMenuDto response = menuService.getMenu(menuId);

        assertThat(response).isNotNull();
    }
}
