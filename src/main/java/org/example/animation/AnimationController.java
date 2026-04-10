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
import org.example.ai.AiService;

@RestController
@RequestMapping("/api/animations")
public class AnimationController {

    private final AnimationService animationService;
    private final AiService aiService;

    // 💡 이 생성자 하나만 남겨두면 스프링이 알아서 두 서비스 모두 주입(Autowired)해 줍니다!
    public AnimationController(AnimationService animationService, AiService aiService) {
        this.animationService = animationService;
        this.aiService = aiService;
    }

    // AI 분석 결과만 미리 확인하는 API
    @PostMapping("/analyze")
    public String analyzeOnly(@RequestBody String originalCode) {
        return aiService.analyzeCode(originalCode);
    }

    @GetMapping
    public List<AnimationSummaryResponse> getAnimations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer size
    ) {
        return animationService.getAnimations(principal, page, size);
    }

    @GetMapping("/{animationId}")
    public AnimationDetailResponse getAnimation(
            @PathVariable Integer animationId,
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