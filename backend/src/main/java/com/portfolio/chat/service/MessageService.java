package com.portfolio.chat.service;

import com.portfolio.chat.dto.MessageResponse;
import com.portfolio.chat.entity.ChatRoom;
import com.portfolio.chat.entity.Message;
import com.portfolio.chat.entity.User;
import com.portfolio.chat.exception.ResourceNotFoundException;
import com.portfolio.chat.repository.ChatRoomRepository;
import com.portfolio.chat.repository.MessageRepository;
import com.portfolio.chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageResponse saveMessage(String content, Long roomId, String senderUsername, Message.MessageType type) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", senderUsername));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));

        Message message = Message.builder()
                .content(content)
                .sender(sender)
                .chatRoom(chatRoom)
                .type(type != null ? type : Message.MessageType.TEXT)
                .build();

        message = messageRepository.save(message);
        log.debug("Message saved in room {} by {}", roomId, senderUsername);

        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesForRoom(Long roomId, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));

        return messageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable)
                .map(this::toResponse);
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getSender().getUsername(),
                message.getSender().getDisplayName(),
                message.getChatRoom().getId(),
                message.getType(),
                message.getCreatedAt()
        );
    }
}
