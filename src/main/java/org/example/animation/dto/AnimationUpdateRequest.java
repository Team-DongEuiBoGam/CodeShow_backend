package org.example.animation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnimationUpdateRequest(
        @NotBlank(message = "애니메이션 이름은 필수입니다.")
        @Size(max = 25, message = "애니메이션 이름은 25자 이하여야 합니다.")
        String animationName
) {
}