package org.example.animation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.example.animation.dto.AnimationCreateRequest;
import org.example.animation.dto.AnimationDetailResponse;
import org.example.animation.dto.AnimationSummaryResponse;
import org.example.animation.dto.AnimationUpdateRequest;
import org.example.auth.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.example.ai.AiService;

@RestController
@RequestMapping("/api/animations")
public class AnimationController {

    private final AnimationService animationService;
    private final AiService aiService;

    public AnimationController(AnimationService animationService, AiService aiService) {
        this.animationService = animationService;
        this.aiService = aiService;
    }

    @Operation(summary = "애니메이션 목록 조회", description = "현재 사용자가 생성한 애니메이션들의 요약 목록을 페이지네이션 형태로 가져옵니다.")
    @GetMapping
    public List<AnimationSummaryResponse> getAnimations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer size
    ) {
        return animationService.getAnimations(principal, page, size);
    }

    @Operation(summary = "애니메이션 상세 조회", description = "특정 ID에 해당하는 애니메이션의 상세 정보와 코드 구조를 가져옵니다.")
    @GetMapping("/{animationId}")
    public AnimationDetailResponse getAnimation(
            @PathVariable Integer animationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return animationService.getAnimation(animationId, principal);
    }

    @Operation(summary = "새 애니메이션 생성", description = "AI 분석을 바탕으로 새로운 코드 애니메이션 데이터를 저장하고 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimationDetailResponse createAnimation(
            @Valid @RequestBody AnimationCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return animationService.createAnimation(request, principal);
    }

    @Operation(summary = "애니메이션 이름 수정", description = "생성된 애니메이션의 이름을 수정합니다.")
    @PatchMapping("/{animationId}")
    public AnimationDetailResponse updateAnimation(
            @PathVariable Integer animationId,
            @Valid @RequestBody AnimationUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return animationService.updateAnimation(animationId, request, principal);
    }

    @Operation(summary = "애니메이션 삭제", description = "저장된 애니메이션을 삭제합니다.")
    @DeleteMapping("/{animationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content 반환
    public void deleteAnimation(
            @PathVariable Integer animationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        animationService.deleteAnimation(animationId, principal);
    }
}