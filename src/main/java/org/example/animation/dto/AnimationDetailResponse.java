package org.example.animation.dto;

import java.time.LocalDate;

public record AnimationDetailResponse(
        Long animationId,
        String animationName,
        String originalCode,
        String jsonData,
        Long languageId,
        String languageName,
        Integer creatorUserNumber,
        String creatorUsername,
        LocalDate createdAt
) {
}
