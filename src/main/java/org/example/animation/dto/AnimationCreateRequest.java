package org.example.animation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnimationCreateRequest(
        @NotBlank(message = "애니메이션 이름은 필수입니다.")
        @Size(max = 25, message = "애니메이션 이름은 25자 이하여야 합니다.")
        String animationName,

        @NotBlank(message = "원본 코드는 필수입니다.")
        String originalCode,

        @NotNull(message = "언어 코드는 필수입니다.")
        Long languageId,

        @NotBlank(message = "JSON 데이터는 필수입니다.")
        String jsonData
) {
}
