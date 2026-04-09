package org.example.auth;

import org.example.auth.dto.AuthResponse;
import org.example.auth.dto.CurrentUserResponse;
import org.example.auth.dto.LoginRequest;
import org.example.auth.dto.SignupRequest;
import org.example.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원가입, 로그인, 비회원 로그인 흐름을 처리한다.
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 같은 아이디가 있으면 회원가입을 막는다.
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new ApiException("이미 사용 중인 아이디입니다.");
        }

        User user = userRepository.save(new User(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.username()
        ));

        return toAuthResponse(user);
    }

    public AuthResponse guestLogin() {
        // 비회원은 DB에 저장하지 않고 임시 이름과 토큰만 발급한다.
        String username = "guest-" + System.currentTimeMillis();
        return new AuthResponse(
                null,
                null,
                username,
                UserRole.GUEST.name(),
                jwtTokenProvider.createAccessToken(null, null, username, UserRole.GUEST)
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // 스프링 시큐리티 인증 매니저로 아이디/비밀번호를 검증한다.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.loginId(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new ApiException("사용자를 찾을 수 없습니다."));

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(CustomUserPrincipal principal) {
        // 회원 토큰으로 들어온 경우 DB에서 현재 회원 정보를 다시 조회한다.
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return new CurrentUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public CurrentUserResponse getCurrentGuest(CustomUserPrincipal principal) {
        // 비회원은 토큰에 담긴 이름만 그대로 내려준다.
        if (principal == null || principal.getRole() != UserRole.GUEST) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "게스트 인증 정보가 없습니다.");
        }

        return new CurrentUserResponse(
                null,
                null,
                principal.getDisplayName(),
                principal.getRole().name()
        );
    }

    private AuthResponse toAuthResponse(User user) {
        // 로그인/회원가입 응답은 기본 회원 정보와 JWT를 함께 내려준다.
        return new AuthResponse(
                user.getId(),
                user.getLoginId(),
                user.getUsername(),
                user.getRole().name(),
                jwtTokenProvider.createAccessToken(
                        user.getId(),
                        user.getLoginId(),
                        user.getUsername(),
                        user.getRole()
                )
        );
    }
}
