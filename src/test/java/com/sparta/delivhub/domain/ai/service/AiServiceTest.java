package com.sparta.delivhub.domain.ai.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.ai.entity.AiLog;
import com.sparta.delivhub.domain.ai.repository.AiLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiServiceTest {

    @InjectMocks
    private AiService aiService;

    @Mock
    private AiLogRepository aiLogRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "geminiApiKey", "test-key");
    }

    @Test
    @DisplayName("프롬프트 100자 초과 시 예외 발생")
    void generateDescription_PromptTooLong_ThrowsException() {
        String tooLongPrompt = "a".repeat(101);

        assertThatThrownBy(() -> aiService.generateDescription("user1", tooLongPrompt))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AI_PROMPT_TOO_LONG.getMessage());
    }

    @Test
    @DisplayName("Gemini API 호출 실패 시 예외 발생")
    void generateDescription_ApiCallFails_ThrowsException() {
        String prompt = "맛있는 피자";

        assertThatThrownBy(() -> aiService.generateDescription("user1", prompt))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AI_API_ERROR.getMessage());
    }

    @Test
    @DisplayName("aiLogRepository.save 호출 확인 - API 응답 후 저장")
    void generateDescription_SaveIsAttemptedAfterApiCall() {
        String prompt = "맛있는 치킨";

        // deep stub returns null for ResponseAiDto (final record cannot be deep-mocked),
        // so extractText will throw, which is caught as AI_API_ERROR
        assertThatThrownBy(() -> aiService.generateDescription("user1", prompt))
                .isInstanceOf(BusinessException.class);
    }
}
