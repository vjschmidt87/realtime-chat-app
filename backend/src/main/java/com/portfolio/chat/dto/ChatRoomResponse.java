package com.portfolio.chat.dto;

import com.portfolio.chat.entity.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        String name,
        String description,
        ChatRoom.RoomType type,
        int memberCount,
        String createdBy,
        LocalDateTime createdAt
) {
}
