package org.example.animation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 애니메이션 메타데이터 저장소
 * fetch join을 사용해 N+1 쿼리 문제를 완화하는 커스텀 조회 메서드를 제공합니다.
 */
public interface AnimationMetadataRepository extends JpaRepository<AnimationMetadata, Long> {

    // 전체 조회 시 language 및 creator를 한 번에 가져오기 위한 fetch join
    @Query("select a from AnimationMetadata a join fetch a.language join fetch a.creator")
    List<AnimationMetadata> findAllWithLanguageAndCreator();

    // 단건 조회 시 language 및 creator를 함께 조회하기 위한 fetch join
    @Query("select a from AnimationMetadata a join fetch a.language join fetch a.creator where a.id = :id")
    Optional<AnimationMetadata> findByIdWithLanguageAndCreator(@Param("id") Long id);
}
