package com.portfolio.chat.dto;

public record MemberResponse(
        Long id,
        String username,
        String displayName,
        String status
) {
}
