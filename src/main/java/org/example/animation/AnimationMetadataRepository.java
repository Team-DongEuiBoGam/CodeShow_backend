package org.example.animation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimationMetadataRepository extends JpaRepository<AnimationMetadata, Long> {

    @Query("select a from AnimationMetadata a join fetch a.language join fetch a.creator")
    List<AnimationMetadata> findAllWithLanguageAndCreator();

    @Query("select a from AnimationMetadata a join fetch a.language join fetch a.creator where a.creator.id = :userId")
    List<AnimationMetadata> findAllByCreatorIdWithLanguageAndCreator(@Param("userId") Integer userId);

    @Query("select a from AnimationMetadata a join fetch a.language join fetch a.creator where a.id = :id")
    Optional<AnimationMetadata> findByIdWithLanguageAndCreator(@Param("id") Long id);
}