package com.sparta.delivhub.domain.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 10)
    private String userId;

    @Column(name = "request_text", nullable = false, length = 100)
    private String requestText;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "request_type", nullable = false, length = 30)
    private String requestType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @Builder
    public AiLog(String userId, String requestText, String responseText, String requestType, String createdBy) {
        this.userId = userId;
        this.requestText = requestText;
        this.responseText = responseText;
        this.requestType = requestType;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }
}
