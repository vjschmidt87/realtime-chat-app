package com.portfolio.chat.dto;

import com.portfolio.chat.entity.User;

import java.time.LocalDateTime;

public record PresenceEvent(
        String username,
        User.Status status,
        LocalDateTime timestamp
) {
}
