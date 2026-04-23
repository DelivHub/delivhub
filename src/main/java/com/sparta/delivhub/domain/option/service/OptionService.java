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
import com.sparta.delivhub.domain.option.repository.OptionRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        Option option = Option.builder()
                .menu(menu)
                .name(request.getName())
                .extraPrice(request.getExtraPrice())
                .build();

        optionRepository.save(option);
        return ResponseOptionDto.from(option);
    }

    // 옵션 목록 조회
    public List<ResponseOptionDto> getOptions(UUID menuId) {
        menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION));

        return optionRepository.findByMenuIdAndDeletedAtIsNull(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();
    }

    // 옵션 수정
    @Transactional
    public ResponseOptionDto updateOption(
            UUID menuId, UUID optionId, UpdateOptionDto request, String username
    ) {
        getMenuAndCheckPermission(menuId, username);

        Option option = optionRepository.findByIdAndDeletedAtIsNull(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.update(request.getName(), request.getExtraPrice());
        return ResponseOptionDto.from(option);
    }

    // 옵션 삭제
    @Transactional
    public void deleteOption(UUID menuId, UUID optionId, String username) {
        getMenuAndCheckPermission(menuId, username);

        Option option = optionRepository.findByIdAndDeletedAtIsNull(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.softDelete(username);
    }
    private Menu getMenuAndCheckPermission(UUID menuId, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION));

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthorizationUtils.checkOwnerOrAdminPermission(user, menu.getStore().getOwner().getUsername());

        return menu;
    }
}