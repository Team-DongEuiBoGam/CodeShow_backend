package org.example.animation;

import java.util.List;
import org.example.animation.dto.AnimationCreateRequest;
import org.example.animation.dto.AnimationDetailResponse;
import org.example.animation.dto.AnimationSummaryResponse;
import org.example.auth.CustomUserPrincipal;
import org.example.auth.User;
import org.example.auth.UserRepository;
import org.example.auth.UserRole;
import org.example.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 애니메이션 조회/저장 권한과 DB 저장 흐름을 담당한다.
@Service
public class AnimationService {

    private final AnimationMetadataRepository animationMetadataRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    public AnimationService(
            AnimationMetadataRepository animationMetadataRepository,
            LanguageRepository languageRepository,
            UserRepository userRepository
    ) {
        this.animationMetadataRepository = animationMetadataRepository;
        this.languageRepository = languageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimationSummaryResponse> getAnimations(CustomUserPrincipal principal) {
        // 회원과 비회원 모두 조회는 가능하다.
        validateViewer(principal);
        return animationMetadataRepository.findAll()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimationDetailResponse getAnimation(Long animationId, CustomUserPrincipal principal) {
        // 상세 조회도 조회 권한만 있으면 가능하다.
        validateViewer(principal);
        AnimationMetadata metadata = animationMetadataRepository.findById(animationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "애니메이션을 찾을 수 없습니다."));
        return toDetailResponse(metadata);
    }

    @Transactional
    public AnimationDetailResponse createAnimation(AnimationCreateRequest request, CustomUserPrincipal principal) {
        // 저장은 로그인한 회원만 허용한다.
        if (principal == null || principal.getRole() != UserRole.USER || principal.getUserId() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "회원만 저장할 수 있습니다.");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "언어를 찾을 수 없습니다."));

        AnimationMetadata metadata = animationMetadataRepository.save(
                new AnimationMetadata(
                        request.animationName(),
                        request.originalCode(),
                        request.jsonData(),
                        user,
                        language
                )
        );

        return toDetailResponse(metadata);
    }

    private void validateViewer(CustomUserPrincipal principal) {
        // 조회는 회원/비회원 둘 다 허용하고, 그 외 비인증 요청은 막는다.
        if (principal == null || (principal.getRole() != UserRole.USER && principal.getRole() != UserRole.GUEST)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "조회 권한이 없습니다.");
        }
    }

    private AnimationSummaryResponse toSummaryResponse(AnimationMetadata metadata) {
        return new AnimationSummaryResponse(
                metadata.getId(),
                metadata.getName(),
                metadata.getLanguage().getId(),
                metadata.getLanguage().getName(),
                metadata.getCreator().getId(),
                metadata.getCreator().getUsername(),
                metadata.getCreatedAt()
        );
    }

    private AnimationDetailResponse toDetailResponse(AnimationMetadata metadata) {
        return new AnimationDetailResponse(
                metadata.getId(),
                metadata.getName(),
                metadata.getOriginalCode(),
                metadata.getJsonData(),
                metadata.getLanguage().getId(),
                metadata.getLanguage().getName(),
                metadata.getCreator().getId(),
                metadata.getCreator().getUsername(),
                metadata.getCreatedAt()
        );
    }
}
