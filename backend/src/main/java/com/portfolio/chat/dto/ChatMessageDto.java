package com.portfolio.chat.dto;

import com.portfolio.chat.entity.Message;
import jakarta.validation.constraints.NotNull;

public record ChatMessageDto(
        String content,

        @NotNull(message = "Room ID is required")
        Long roomId,

        Message.MessageType type
) {
}
