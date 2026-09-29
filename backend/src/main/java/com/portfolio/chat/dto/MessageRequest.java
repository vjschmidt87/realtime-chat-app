package com.portfolio.chat.dto;

import com.portfolio.chat.entity.Message;
import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank(message = "Message content is required")
        String content,

        Message.MessageType type
) {
}
