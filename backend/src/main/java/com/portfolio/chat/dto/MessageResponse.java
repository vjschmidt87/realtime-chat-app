package com.portfolio.chat.dto;

import com.portfolio.chat.entity.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String content,
        String senderUsername,
        String senderDisplayName,
        Long roomId,
        Message.MessageType type,
        LocalDateTime createdAt
) {
}
