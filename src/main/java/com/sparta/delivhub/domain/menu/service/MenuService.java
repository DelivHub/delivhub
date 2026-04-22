package com.sparta.delivhub.domain.menu.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
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
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
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

    // 메뉴 등록
    @Transactional
    public ResponseMenuDto createMenu(UUID storeId, CreateMenuDto request, String username) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        String description = request.getDescription();

        // AI 설명 생성
        if (Boolean.TRUE.equals(request.getAiDescription())) {
            String aiPrompt = request.getAiPrompt() + " 50자 이하로";
            // 추후 AiService 주입 후 구현
            // description = aiService.generateDescription(username, request.getAiPrompt());
        }

        Menu menu = Menu.builder()
                .store(store)
                .name(request.getName())
                .price(request.getPrice())
                .description(description)
                .build();

        menuRepository.save(menu);

        // 옵션 같이 생성
        List<Option> options = new ArrayList<>();
        if (request.getOptions() != null) {
            for (CreateOptionDto optionDto : request.getOptions()) {
                Option option = Option.builder()
                        .menu(menu)
                        .name(optionDto.getName())
                        .extraPrice(optionDto.getExtraPrice())
                        .build();
                options.add(option);
            }
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

        List<ResponseOptionDto> options = optionRepository.findByMenuIdAndDeletedAtIsNull(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();

        return ResponseMenuDto.from(menu, options);
    }

    // 메뉴 수정
    @Transactional
    public ResponseMenuDto updateMenu(UUID menuId, UpdateMenuDto request, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_UPDATE));

        menu.update(request.getName(), request.getPrice(), request.getDescription());

        List<ResponseOptionDto> options = optionRepository.findByMenuIdAndDeletedAtIsNull(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();

        return ResponseMenuDto.from(menu, options);
    }

    // 메뉴 숨김 처리
    @Transactional
    public void updateMenuHidden(UUID menuId, HiddenMenuDto request, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_UPDATE_STATUS));

        menu.updateHidden(request.getIsHidden());
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_DELETE));

        menu.softDelete(username);
    }
}
