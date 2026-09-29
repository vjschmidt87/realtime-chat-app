package com.portfolio.chat.controller;

import com.portfolio.chat.dto.ChatMessageDto;
import com.portfolio.chat.dto.MessageResponse;
import com.portfolio.chat.dto.PresenceEvent;
import com.portfolio.chat.entity.User;
import com.portfolio.chat.service.MessageService;
import com.portfolio.chat.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final PresenceService presenceService;

    public WebSocketChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService,
                                   PresenceService presenceService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.presenceService = presenceService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto chatMessage, Principal principal) {
        String username = extractUsername(principal);
        log.debug("Received message from {} to room {}", username, chatMessage.roomId());

        MessageResponse saved = messageService.saveMessage(
                chatMessage.content(),
                chatMessage.roomId(),
                username,
                chatMessage.type()
        );

        messagingTemplate.convertAndSend("/topic/room." + chatMessage.roomId(), saved);
    }

    @MessageMapping("/chat.presence")
    public void handlePresence(@Payload PresenceEvent event, Principal principal) {
        String username = extractUsername(principal);
        log.debug("Presence event from {}: {}", username, event.status());

        switch (event.status()) {
            case ONLINE -> presenceService.userConnected(username);
            case OFFLINE -> presenceService.userDisconnected(username);
            case AWAY -> presenceService.userAway(username);
        }
    }

    private String extractUsername(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            Object p = authToken.getPrincipal();
            if (p instanceof User user) {
                return user.getUsername();
            }
        }
        return principal.getName();
    }
}
