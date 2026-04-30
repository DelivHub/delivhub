package com.sparta.delivhub.domain.ai.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.ai.dto.ResponseAiLogDto;
import com.sparta.delivhub.domain.ai.entity.AiLog;
import com.sparta.delivhub.domain.ai.repository.AiLogRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiLogServiceTest {

    @InjectMocks
    private AiLogService aiLogService;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("AI 로그 전체 조회 성공 - 전체 조회")
    void getAiLogs_AllLogs_Success() {
        // given
        String adminUsername = "master1";
        User admin = User.builder().username(adminUsername).userRole(UserRole.MASTER).build();
        Pageable pageable = PageRequest.of(0, 10);
        AiLog log = AiLog.builder()
                .userId("user1")
                .requestText("치킨")
                .responseText("바삭한 치킨")
                .requestType("PRODUCT_DESCRIPTION")
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(adminUsername)).willReturn(Optional.of(admin));
        given(aiLogRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(log)));

        // when
        Page<ResponseAiLogDto> result = aiLogService.getAiLogs(adminUsername, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(aiLogRepository).findAll(pageable);
    }

    @Test
    @DisplayName("AI 로그 전체 조회 성공 - 특정 사용자 필터링")
    void getAiLogs_FilterByUsername_Success() {
        // given
        String adminUsername = "master1";
        String targetUsername = "user1";
        User admin = User.builder().username(adminUsername).userRole(UserRole.MASTER).build();
        Pageable pageable = PageRequest.of(0, 10);

        given(userRepository.findByUsernameAndDeletedAtIsNull(adminUsername)).willReturn(Optional.of(admin));
        given(aiLogRepository.findByUserId(targetUsername, pageable)).willReturn(Page.empty());

        // when
        Page<ResponseAiLogDto> result = aiLogService.getAiLogs(adminUsername, targetUsername, pageable);

        // then
        assertThat(result).isEmpty();
        verify(aiLogRepository).findByUserId(targetUsername, pageable);
    }

    @Test
    @DisplayName("AI 로그 전체 조회 실패 - 권한 없는 사용자")
    void getAiLogs_NonAdminUser_ThrowsException() {
        // given
        String customerUsername = "customer1";
        User customer = User.builder().username(customerUsername).userRole(UserRole.CUSTOMER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(customerUsername)).willReturn(Optional.of(customer));

        // when & then
        assertThatThrownBy(() -> aiLogService.getAiLogs(customerUsername, null, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("AI 로그 단건 조회 성공")
    void getAiLog_Success() {
        // given
        String adminUsername = "master1";
        UUID logId = UUID.randomUUID();
        User admin = User.builder().username(adminUsername).userRole(UserRole.MASTER).build();
        AiLog log = mock(AiLog.class);
        given(log.getId()).willReturn(logId);
        given(log.getUserId()).willReturn("user1");
        given(log.getRequestText()).willReturn("피자");
        given(log.getResponseText()).willReturn("맛있는 피자");
        given(log.getRequestType()).willReturn("PRODUCT_DESCRIPTION");
        given(log.getCreatedAt()).willReturn(LocalDateTime.now());

        given(userRepository.findByUsernameAndDeletedAtIsNull(adminUsername)).willReturn(Optional.of(admin));
        given(aiLogRepository.findById(logId)).willReturn(Optional.of(log));

        // when
        ResponseAiLogDto result = aiLogService.getAiLog(adminUsername, logId);

        // then
        assertThat(result).isNotNull();
        verify(aiLogRepository).findById(logId);
    }

    @Test
    @DisplayName("AI 로그 단건 조회 실패 - 존재하지 않는 로그")
    void getAiLog_NotFound_ThrowsException() {
        // given
        String adminUsername = "master1";
        UUID logId = UUID.randomUUID();
        User admin = User.builder().username(adminUsername).userRole(UserRole.MASTER).build();

        given(userRepository.findByUsernameAndDeletedAtIsNull(adminUsername)).willReturn(Optional.of(admin));
        given(aiLogRepository.findById(logId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> aiLogService.getAiLog(adminUsername, logId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AI_LOG_NOT_FOUND.getMessage());
    }
}
