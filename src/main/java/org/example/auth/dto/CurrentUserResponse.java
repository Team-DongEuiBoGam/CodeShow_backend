package org.example.auth.dto;

public record CurrentUserResponse(
        Integer userId,
        String loginId,
        String username,
        String role
) {
}
