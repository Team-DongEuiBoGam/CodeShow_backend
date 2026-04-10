package org.example.auth.dto;

public record AuthResponse(
        Long userId,
        String loginId,
        String username,
        String role,
        String accessToken
) {
}
