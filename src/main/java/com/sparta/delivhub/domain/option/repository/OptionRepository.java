package com.sparta.delivhub.domain.option.repository;

import com.sparta.delivhub.domain.option.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptionRepository extends JpaRepository<Option, UUID> {
    // 메뉴별 옵션 그룹 목록 조회(삭제x)
    List<Option> findByMenuIdAndDeletedAtIsNull(UUID menuId);

    // 단건 옵션 그룹 조회(삭제x)
    Optional<Option> findByIdAndDeletedAtIsNull(UUID id);

    // 메뉴에 속한 특정 옵션 그룹 조회(삭제x)
    Optional<Option> findByIdAndMenuIdAndDeletedAtIsNull(UUID id, UUID menuId);

    // 메뉴별 옵션 그룹 + 옵션 아이템 함께 조회
    @Query("""
        select distinct o
        from Option o
        left join fetch o.optionItems oi
        where o.menu.id = :menuId
          and o.deletedAt is null
          and (oi.deletedAt is null or oi.id is null)
    """)
    List<Option> findAllByMenuIdWithItems(@Param("menuId") UUID menuId);

    @Query("""
        select distinct o
        from Option o
        left join fetch o.optionItems oi
        where o.id = :optionId
          and o.menu.id = :menuId
          and o.deletedAt is null
          and (oi.deletedAt is null or oi.id is null)
    """)
    Optional<Option> findByIdAndMenuIdWithItems(
            @Param("optionId") UUID optionId,
            @Param("menuId") UUID menuId
    );
}