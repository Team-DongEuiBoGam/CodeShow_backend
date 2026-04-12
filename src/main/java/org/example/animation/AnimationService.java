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
import org.example.common.ImageNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 애니메이션 조회/저장 권한과 DB 저장 흐름을 담당한다.
@Service
public class AnimationService {

    private final AnimationMetadataRepository animationMetadataRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AnimationService(
            AnimationMetadataRepository animationMetadataRepository,
            LanguageRepository languageRepository,
            UserRepository userRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper
    ) {
        this.animationMetadataRepository = animationMetadataRepository;
        this.languageRepository = languageRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AnimationSummaryResponse> getAnimations(CustomUserPrincipal principal, Integer page, Integer size) {
        // 🌟 수정된 부분 1: 로그인한 회원(USER)인지 먼저 검증
        if (principal == null || principal.getRole() != UserRole.USER || principal.getUserId() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "로그인한 회원만 본인의 저장 목록을 조회할 수 있습니다.");
        }

        // 🌟 수정된 부분 2: 본인의 userId를 넘겨서 본인이 작성한 목록만 가져옴
        List<AnimationMetadata> all = animationMetadataRepository.findAllByCreatorIdWithLanguageAndCreator(principal.getUserId());

        if (page == null || size == null) {
            return all.stream().map(this::toSummaryResponse).toList();
        }

        int p = Math.max(0, page);
        int s = Math.max(1, size);
        int fromIndex = p * s;
        if (fromIndex >= all.size()) {
            return java.util.List.of();
        }
        int toIndex = Math.min(all.size(), fromIndex + s);
        return all.subList(fromIndex, toIndex).stream().map(this::toSummaryResponse).toList();
    }

    @Transactional(readOnly = true)
    public AnimationDetailResponse getAnimation(Integer animationId, CustomUserPrincipal principal) {
        // 🌟 수정된 부분 3: 상세 조회도 회원만 가능하도록 권한 체크
        if (principal == null || principal.getRole() != UserRole.USER || principal.getUserId() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
        }

        AnimationMetadata metadata = animationMetadataRepository.findByIdWithLanguageAndCreator((long) animationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "애니메이션을 찾을 수 없습니다."));

        // 🌟 수정된 부분 4: 조회하려는 애니메이션의 작성자가 현재 요청한 사용자가 맞는지 확인
        if (!metadata.getCreator().getId().equals(principal.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "본인이 저장한 애니메이션만 볼 수 있습니다.");
        }

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

        if (request.originalCode().matches("(?i).*\\.(png|jpg|jpeg|gif|bmp)$")) {
            throw new ImageNotSupportedException();
        }

        // jsonData가 유효한 JSON인지 검증
        try {
            objectMapper.readTree(request.jsonData());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "jsonData가 유효한 JSON이 아닙니다.");
        }

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
