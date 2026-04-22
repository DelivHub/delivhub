package com.sparta.delivhub.domain.ai.dto;

import com.sparta.delivhub.domain.ai.entity.AiLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResponseAiLogDto {
    private UUID aiLogId;
    private String userId;
    private String requestText;
    private String responseText;
    private String requestType;
    private LocalDateTime createdAt;
    private String createdBy;

    public static ResponseAiLogDto from(AiLog aiLog) {
        return ResponseAiLogDto.builder()
                .aiLogId(aiLog.getId())
                .userId(aiLog.getUserId())
                .requestText(aiLog.getRequestText())
                .responseText(aiLog.getResponseText())
                .requestType(aiLog.getRequestType())
                .createdAt(aiLog.getCreatedAt())
                .createdBy(aiLog.getCreatedBy())
                .build();
    }
}
