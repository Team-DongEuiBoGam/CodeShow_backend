package org.example.auth.dto;

public record CurrentUserResponse(
        Long userId,
        String loginId,
        String username,
        String role
) {
}
