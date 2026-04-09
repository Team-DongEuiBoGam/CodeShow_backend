package org.example.auth;

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

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/guest-login")
    public AuthResponse guestLogin() {
        return authService.guestLogin();
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.GUEST) {
            return authService.getCurrentGuest(principal);
        }

        return authService.getCurrentUser(principal);
    }
}
