package com.sparta.delivhub.domain.menu.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.util.AuthorizationUtils;
import com.sparta.delivhub.domain.ai.service.AiService;
import com.sparta.delivhub.domain.menu.dto.CreateMenuDto;
import com.sparta.delivhub.domain.menu.dto.HiddenMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuDto;
import com.sparta.delivhub.domain.menu.dto.ResponseMenuListDto;
import com.sparta.delivhub.domain.menu.dto.UpdateMenuDto;
import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import com.sparta.delivhub.domain.option.entity.Option;
import com.sparta.delivhub.domain.option.entity.OptionItem;
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {
    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    // 메뉴 등록
    @Transactional
    public ResponseMenuDto createMenu(UUID storeId, CreateMenuDto request, String username) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인
        AuthorizationUtils.checkOwnerOrAdminPermission(user, store.getOwner().getUsername());

        // AI 설명 생성
        String description = request.getDescription();
        if (Boolean.TRUE.equals(request.getAiDescription())) {
            if (request.getAiPrompt() == null || request.getAiPrompt().isBlank()) {
                throw new BusinessException(ErrorCode.AI_PROMPT_REQUIRED);
            }
            description = aiService.generateDescription(username, request.getAiPrompt());
        }

        Menu menu = Menu.builder()
                .store(store)
                .name(request.getName())
                .price(request.getPrice())
                .description(description)
                .build();

        menuRepository.save(menu);

        // 옵션 같이 생성
        List<Option> options = createOptions(menu, request.getOptions());

        if (!options.isEmpty()) {
            optionRepository.saveAll(options);
        }

        List<ResponseOptionDto> optionDtos = options.stream()
                .map(ResponseOptionDto::from)
                .toList();


        return ResponseMenuDto.from(menu, optionDtos);
    }

    // 메뉴 목록 조회
    public Page<ResponseMenuListDto> getMenus(UUID storeId, Pageable pageable, boolean isAdmin) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Page<Menu> menus;
        if (isAdmin) {
            menus = menuRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable);
        } else {
            menus = menuRepository.findByStoreIdAndIsHiddenFalseAndDeletedAtIsNull(storeId, pageable);
        }

        return menus.map(ResponseMenuListDto::from);
    }

    // 메뉴 단건 조회
    public ResponseMenuDto getMenu(UUID menuId) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_READ));

        List<ResponseOptionDto> options = optionRepository.findAllByMenuIdWithItems(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();

        return ResponseMenuDto.from(menu, options);
    }

    // 메뉴 수정
    @Transactional
    public ResponseMenuDto updateMenu(UUID menuId, UpdateMenuDto request, String username) {
        Menu menu = getMenuAndCheckPermission(menuId, username, ErrorCode.MENU_NOT_FOUND_ON_UPDATE);

        // 수정
        menu.update(request.getName(), request.getPrice(), request.getDescription());

        List<ResponseOptionDto> options = optionRepository.findAllByMenuIdWithItems(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();

        return ResponseMenuDto.from(menu, options);
    }

    // 메뉴 숨김 처리
    @Transactional
    public void updateMenuHidden(UUID menuId, HiddenMenuDto request, String username) {
        Menu menu = getMenuAndCheckPermission(menuId, username, ErrorCode.MENU_NOT_FOUND_ON_UPDATE_STATUS);
        menu.updateHidden(request.getIsHidden());
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId, String username) {
        Menu menu = getMenuAndCheckPermission(menuId, username, ErrorCode.MENU_NOT_FOUND_ON_DELETE);
        menu.softDelete(username);
    }

    // 메뉴 생성 시 옵션 그룹 + 옵션 아이템 생성
    private List<Option> createOptions(Menu menu, List<CreateOptionDto> optionRequests) {
        if (optionRequests == null || optionRequests.isEmpty()) {
            return List.of();
        }

        return optionRequests.stream()
                .map(optionRequest -> createOption(menu, optionRequest))
                .toList();
    }
    // 옵션 그룹 생성
    private Option createOption(Menu menu, CreateOptionDto optionRequest) {
        Option option = Option.builder()
                .menu(menu)
                .name(optionRequest.getName())
                .type(optionRequest.getType())
                .build();

        for (CreateOptionDto.Item itemRequest : optionRequest.getItems()) {
            OptionItem optionItem = OptionItem.builder()
                    .name(itemRequest.getName())
                    .extraPrice(itemRequest.getExtraPrice())
                    .build();

            option.addOptionItem(optionItem);
        }

        return option;
    }

    // 메뉴 조회 + 권한 체크
    private Menu getMenuAndCheckPermission(UUID menuId, String username, ErrorCode menuNotFoundError) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(menuNotFoundError));
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkOwnerOrAdminPermission(user, menu.getStore().getOwner().getUsername());

        return menu;
    }
}
