package org.example.auth;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.auth.dto.AuthResponse;
import org.example.auth.dto.CurrentUserResponse;
import org.example.auth.dto.LoginRequest;
import org.example.auth.dto.SignupRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새로운 계정을 만듭니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @Operation(summary = "로그인", description = "생성된 계정으로 로그인합니다.")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "비회원 로그인", description = "계정 없이 로그인합니다.")
    @PostMapping("/guest-login")
    public AuthResponse guestLogin() {
        return authService.guestLogin();
    }

    @Operation(summary = "계정 상태 확인", description = "로그인 상태인지 비회원 상태인지 체크합니다.")
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.GUEST) {
            return authService.getCurrentGuest(principal);
        }

        return authService.getCurrentUser(principal);
    }
}
