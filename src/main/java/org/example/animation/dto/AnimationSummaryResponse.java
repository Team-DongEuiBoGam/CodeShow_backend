package org.example.animation.dto;

import java.time.LocalDate;

public record AnimationSummaryResponse(
        Long animationId,
        String animationName,
        Long languageId,
        String languageName,
        Integer creatorUserNumber,
        String creatorUsername,
        LocalDate createdAt
) {
}
