package com.sparta.delivhub.domain.ai.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.common.util.AuthorizationUtils;
import com.sparta.delivhub.domain.ai.dto.ResponseAiLogDto;
import com.sparta.delivhub.domain.ai.entity.AiLog;
import com.sparta.delivhub.domain.ai.repository.AiLogRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiLogService {
    private final AiLogRepository aiLogRepository;
    private final UserRepository userRepository;

    // AI 로그 전체 조회
    public Page<ResponseAiLogDto> getAiLogs(String requestUsername, String username, Pageable pageable) {
        checkAdminUser(requestUsername);

        if (username != null) {
            return aiLogRepository.findByUserId(username, pageable)
                    .map(ResponseAiLogDto::from);
        }
        return aiLogRepository.findAll(pageable)
                .map(ResponseAiLogDto::from);
    }

    // AI 로그 단건 조회
    public ResponseAiLogDto getAiLog(String requestUsername, UUID logId) {
        checkAdminUser(requestUsername);

        AiLog aiLog = aiLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_LOG_NOT_FOUND));
        return ResponseAiLogDto.from(aiLog);
    }

    // 권한 체크
    private void checkAdminUser(String username) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        AuthorizationUtils.checkAdminPermission(user);
    }
}
