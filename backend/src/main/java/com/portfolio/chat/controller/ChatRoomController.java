package com.portfolio.chat.controller;

import com.portfolio.chat.dto.ChatRoomRequest;
import com.portfolio.chat.dto.ChatRoomResponse;
import com.portfolio.chat.dto.MemberResponse;
import com.portfolio.chat.entity.User;
import com.portfolio.chat.service.ChatRoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getUserRooms(@AuthenticationPrincipal User user) {
        List<ChatRoomResponse> rooms = chatRoomService.getUserRooms(user.getUsername());
        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createRoom(
            @Valid @RequestBody ChatRoomRequest request,
            @AuthenticationPrincipal User user) {
        ChatRoomResponse room = chatRoomService.createRoom(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoomResponse> getRoom(@PathVariable Long id) {
        ChatRoomResponse room = chatRoomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ChatRoomResponse> joinRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ChatRoomResponse room = chatRoomService.joinRoom(id, user.getUsername());
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        chatRoomService.leaveRoom(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MemberResponse>> getRoomMembers(@PathVariable Long id) {
        List<MemberResponse> members = chatRoomService.getRoomMembers(id);
        return ResponseEntity.ok(members);
    }
}
