package com.sparta.delivhub.domain.menu.repository;

import com.sparta.delivhub.domain.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {
    // 가게별 메뉴 목록 조회(삭제x)
    Page<Menu> findByStoreIdAndDeletedAtIsNull(UUID storeId, Pageable pageable);

    // 가게별 메뉴 목록 조회(숨김x + 삭제x)
    Page<Menu> findByStoreIdAndIsHiddenFalseAndDeletedAtIsNull(UUID storeId, Pageable pageable);

    // 단건 조회(삭제x)
    Optional<Menu> findByIdAndDeletedAtIsNull(UUID id);
}