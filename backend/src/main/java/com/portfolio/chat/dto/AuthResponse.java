package com.portfolio.chat.dto;

public record AuthResponse(
        String token,
        String username,
        String displayName,
        String email
) {
}
