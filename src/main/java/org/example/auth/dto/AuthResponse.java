package org.example.auth.dto;

public record AuthResponse(
        Integer userId,
        String loginId,
        String username,
        String role,
        String accessToken
) {
}
