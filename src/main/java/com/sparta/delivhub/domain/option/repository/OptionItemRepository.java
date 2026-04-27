package com.sparta.delivhub.domain.option.repository;

import com.sparta.delivhub.domain.option.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OptionItemRepository extends JpaRepository<OptionItem, UUID> {
    // 주문 생성 시 선택한 옵션 아이템 목록 조회
    List<OptionItem> findByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}