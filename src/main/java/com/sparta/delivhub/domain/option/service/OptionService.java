package com.sparta.delivhub.domain.option.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
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

import static com.sparta.delivhub.domain.user.entity.UserRole.OWNER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {
    private final OptionRepository optionRepository;
    private final MenuRepository menuRepository;

    // 옵션 등록
    @Transactional
    public ResponseOptionDto createOption(UUID menuId, CreateOptionDto request, String username) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION_CREATE));

        // OWNER면 본인 가게 메뉴인지 확인 (추후 Security 완성되면 role 체크 추가)
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
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION_READ));

        return optionRepository.findByMenuIdAndDeletedAtIsNull(menuId)
                .stream()
                .map(ResponseOptionDto::from)
                .toList();
    }

    // 옵션 수정
    @Transactional
    public ResponseOptionDto updateOption(UUID menuId, UUID optionId, UpdateOptionDto request, String username) {
        menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION_READ));

        Option option = optionRepository.findByIdAndDeletedAtIsNull(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.update(request.getName(), request.getExtraPrice());
        return ResponseOptionDto.from(option);
    }

    // 옵션 삭제
    @Transactional
    public void deleteOption(UUID menuId, UUID optionId, String username) {
        menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND_ON_OPTION_READ));

        Option option = optionRepository.findByIdAndDeletedAtIsNull(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        option.softDelete(username);
    }
}