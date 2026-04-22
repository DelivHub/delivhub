package com.sparta.delivhub.domain.option.repository;

import com.sparta.delivhub.domain.option.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptionRepository extends JpaRepository<Option, UUID> {
    // 메뉴별 옵션 목록 조회(삭제x)
    List<Option> findByMenuIdAndDeletedAtIsNull(UUID menuId);

    // 단건 조회(삭제x)
    Optional<Option> findByIdAndDeletedAtIsNull(UUID id);
}