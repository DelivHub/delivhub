package com.sparta.delivhub.domain.ai.repository;

import com.sparta.delivhub.domain.ai.entity.AiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiLogRepository extends JpaRepository<AiLog, UUID> {
    // 특정 유저 로그 조회
    Page<AiLog> findByUserId(String userId, Pageable pageable);
}
