package com.sparta.delivhub.domain.ai.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.ai.dto.ResponseAiDto;
import com.sparta.delivhub.domain.ai.entity.AiLog;
import com.sparta.delivhub.domain.ai.repository.AiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
    private final AiLogRepository aiLogRepository;
    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String SUFFIX_PROMPT = " 배달음식 플랫폼의 메뉴 소개글처럼 작성하고, "
            + "고객이 바로 이해할 수 있게 자연스럽고 간결한 한 줄로 50자 이하로 답변해줘. "
            + "마크다운 문법, 글자 수 표기 없이 순수 텍스트로만 답변해줘.";

    public String generateDescription(String userId, String prompt) {
        if (prompt.length() > 100) {
            throw new BusinessException(ErrorCode.AI_PROMPT_TOO_LONG);
        }
        String fullPrompt = prompt + SUFFIX_PROMPT;


        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", fullPrompt)
                        ))
                )
        );

        ResponseAiDto response;
        try {
            response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(ResponseAiDto.class);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_API_ERROR);
        }

        String result = extractText(response);

        aiLogRepository.save(AiLog.builder()
                .userId(userId)
                .requestText(prompt)
                .responseText(result)
                .requestType("PRODUCT_DESCRIPTION")
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build());

        return result;
    }

    public String generateDescriptionFromImage(String userId, MultipartFile image) {
        // 이미지 형식 검증
        String contentType = image.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new BusinessException(ErrorCode.AI_IMAGE_INVALID);
        }

        // base64 변환
        String base64Image;
        try {
            base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        } catch (IOException e) {
            log.error("이미지 변환 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_API_ERROR);
        }

        String prompt = "이 음식 이미지를 보고" + SUFFIX_PROMPT;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("inlineData", Map.of(
                                        "mimeType", contentType,
                                        "data", base64Image
                                )),
                                Map.of("text", prompt)
                        ))
                )
        );

        ResponseAiDto response;

        try {
            response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(ResponseAiDto.class);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_API_ERROR);
        }

        String result = extractText(response);

        aiLogRepository.save(AiLog.builder()
                .userId(userId)
                .requestText("이미지 기반 메뉴 설명 생성")
                .responseText(result)
                .requestType("IMAGE_DESCRIPTION")
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build());

        return result;
    }

    private String extractText(ResponseAiDto response) {
        try {
            return response.candidates().get(0)
                    .content().parts().get(0)
                    .text();
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_API_ERROR);
        }
    }
}