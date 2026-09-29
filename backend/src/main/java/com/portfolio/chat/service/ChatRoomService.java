package com.portfolio.chat.service;

import com.portfolio.chat.dto.ChatRoomRequest;
import com.portfolio.chat.dto.ChatRoomResponse;
import com.portfolio.chat.dto.MemberResponse;
import com.portfolio.chat.entity.ChatRoom;
import com.portfolio.chat.entity.User;
import com.portfolio.chat.exception.ResourceNotFoundException;
import com.portfolio.chat.repository.ChatRoomRepository;
import com.portfolio.chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ChatRoomService {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomService.class);

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatRoomResponse createRoom(ChatRoomRequest request, String username) {
        User creator = findUserByUsername(username);

        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type() != null ? request.type() : ChatRoom.RoomType.GROUP)
                .createdBy(creator)
                .build();

        chatRoom.getMembers().add(creator);
        chatRoom = chatRoomRepository.save(chatRoom);
        log.info("Chat room created: '{}' by {}", chatRoom.getName(), username);

        return toResponse(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserRooms(String username) {
        User user = findUserByUsername(username);

        return chatRoomRepository.findAllByMember(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomById(Long roomId) {
        ChatRoom room = findRoomWithMembers(roomId);
        return toResponse(room);
    }

    @Transactional
    public ChatRoomResponse joinRoom(Long roomId, String username) {
        ChatRoom room = findRoomWithMembers(roomId);
        User user = findUserByUsername(username);

        room.getMembers().add(user);
        room = chatRoomRepository.save(room);
        log.info("User {} joined room '{}'", username, room.getName());

        return toResponse(room);
    }

    @Transactional
    public ChatRoomResponse leaveRoom(Long roomId, String username) {
        ChatRoom room = findRoomWithMembers(roomId);
        User user = findUserByUsername(username);

        room.getMembers().remove(user);
        room = chatRoomRepository.save(room);
        log.info("User {} left room '{}'", username, room.getName());

        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getRoomMembers(Long roomId) {
        ChatRoom room = findRoomWithMembers(roomId);
        return room.getMembers().stream()
                .map(this::toMemberResponse)
                .toList();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private ChatRoom findRoomWithMembers(Long roomId) {
        return chatRoomRepository.findByIdWithMembers(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", roomId));
    }

    private ChatRoomResponse toResponse(ChatRoom room) {
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getType(),
                room.getMembers().size(),
                room.getCreatedBy().getUsername(),
                room.getCreatedAt()
        );
    }

    private MemberResponse toMemberResponse(User member) {
        return new MemberResponse(
                member.getId(),
                member.getUsername(),
                member.getDisplayName() != null ? member.getDisplayName() : member.getUsername(),
                member.getStatus().name()
        );
    }
}
