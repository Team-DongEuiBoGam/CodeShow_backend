package org.example.animation;

import jakarta.validation.Valid;
import java.util.List;
import org.example.animation.dto.AnimationCreateRequest;
import org.example.animation.dto.AnimationDetailResponse;
import org.example.animation.dto.AnimationSummaryResponse;
import org.example.auth.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/animations")
public class AnimationController {

    private final AnimationService animationService;

    public AnimationController(AnimationService animationService) {
        this.animationService = animationService;
    }

    @GetMapping
    public List<AnimationSummaryResponse> getAnimations(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return animationService.getAnimations(principal);
    }

    @GetMapping("/{animationId}")
    public AnimationDetailResponse getAnimation(
            @PathVariable Long animationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return animationService.getAnimation(animationId, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimationDetailResponse createAnimation(
            @Valid @RequestBody AnimationCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return animationService.createAnimation(request, principal);
    }
}
