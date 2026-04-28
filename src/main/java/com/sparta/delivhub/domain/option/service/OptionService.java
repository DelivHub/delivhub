package com.sparta.delivhub.domain.option.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.util.AuthorizationUtils;
import com.sparta.delivhub.domain.menu.entity.Menu;
import com.sparta.delivhub.domain.menu.repository.MenuRepository;
import com.sparta.delivhub.domain.option.dto.CreateOptionDto;
import com.sparta.delivhub.domain.option.dto.ResponseOptionDto;
import com.sparta.delivhub.domain.option.dto.UpdateOptionDto;
import com.sparta.delivhub.domain.option.entity.Option;
import com.sparta.delivhub.domain.option.entity.OptionItem;
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {
    private final OptionRepository optionRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    // 옵션 등록
    @Transactional
    public ResponseOptionDto createOption(UUID menuId, CreateOptionDto request, String username) {
        Menu menu = getMenuAndCheckPermission(menuId, username);

        // 옵션 그룹 생성
        Option option = Option.builder()
                .menu(menu)
                .name(request.getName())
                .type(request.getType())
                .build();

        // 옵션 아이템 생성
        for (CreateOptionDto.Item itemRequest : request.getItems()) {
            OptionItem optionItem = OptionItem.builder()
                    .name(itemRequest.getName())
                    .extraPrice(itemRequest.getExtraPrice())
                    .build();

            option.addOptionItem(optionItem);
        }

        optionRepository.save(option);
        return ResponseOptionDto.from(option);
    }

    // 옵션 그룹 목록 조회
    public List<ResponseOptionDto> getOptions(UUID menuId) {
        menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION));

        return optionRepository.findAllByMenuIdWithItems(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();
    }

    // 옵션 그룹 + 옵션 아이템 수정
    @Transactional
    public ResponseOptionDto updateOption(
            UUID menuId, UUID optionId, UpdateOptionDto request, String username
    ) {
        getMenuAndCheckPermission(menuId, username);

        Option option = optionRepository.findByIdAndMenuIdAndDeletedAtIsNull(optionId, menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.update(request.getName(), request.getType());

        if (request.getItems() != null) {
            updateOptionItems(option, request.getItems(), username);
        }

        return ResponseOptionDto.from(option);
    }


    // 옵션 아이템 수정/추가/삭제
    private void updateOptionItems(Option option, List<UpdateOptionDto.Item> itemRequests, String username) {
        List<UUID> requestItemIds = itemRequests.stream()
                .map(UpdateOptionDto.Item::getOptionItemId)
                .filter(Objects::nonNull)
                .toList();

        // 요청에 없는 기존 아이템은 삭제
        option.getOptionItems().stream()
                .filter(optionItem -> optionItem.getDeletedAt() == null)
                .filter(optionItem -> !requestItemIds.contains(optionItem.getId()))
                .forEach(optionItem -> optionItem.softDelete(username));


        for (UpdateOptionDto.Item itemRequest : itemRequests) {
            if (itemRequest.getOptionItemId() == null) {
                addNewOptionItem(option, itemRequest);
            } else {
                updateExistingOptionItem(option, itemRequest);
            }
        }
    }

    // 옵션 삭제
    @Transactional
    public void deleteOption(UUID menuId, UUID optionId, String username) {
        getMenuAndCheckPermission(menuId, username);

        Option option = optionRepository.findByIdAndMenuIdWithItems(optionId, menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.softDelete(username);
        option.getOptionItems().stream()
                .filter(optionItem -> optionItem.getDeletedAt() == null)
                .forEach(optionItem -> optionItem.softDelete(username));
    }

    // 새 옵션 아이템 추가
    private void addNewOptionItem(Option option, UpdateOptionDto.Item itemRequest) {
        OptionItem optionItem = OptionItem.builder()
                .name(itemRequest.getName())
                .extraPrice(itemRequest.getExtraPrice())
                .build();

        option.addOptionItem(optionItem);
    }

    // 기존 옵션 아이템 수정
    private void updateExistingOptionItem(Option option, UpdateOptionDto.Item itemRequest) {
        OptionItem optionItem = option.getOptionItems().stream()
                .filter(item -> item.getDeletedAt() == null)
                .filter(item -> item.getId().equals(itemRequest.getOptionItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_ITEM_NOT_FOUND));

        optionItem.update(itemRequest.getName(), itemRequest.getExtraPrice());
    }


    // 메뉴 조회 + 권한 체크
    private Menu getMenuAndCheckPermission(UUID menuId, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION));

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkOwnerOrAdminPermission(user, menu.getStore().getOwner().getUsername());

        return menu;
    }
}