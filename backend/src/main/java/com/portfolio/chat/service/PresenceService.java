package com.portfolio.chat.service;

import com.portfolio.chat.dto.PresenceEvent;
import com.portfolio.chat.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    private static final String ONLINE_USERS_KEY = "chat:online_users";

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public PresenceService(StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate,
                           UserService userService) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    public void userConnected(String username) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, username);
        userService.updateStatus(username, User.Status.ONLINE);

        PresenceEvent event = new PresenceEvent(username, User.Status.ONLINE, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/presence", event);
        log.info("User connected: {}", username);
    }

    public void userDisconnected(String username) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, username);
        userService.updateStatus(username, User.Status.OFFLINE);

        PresenceEvent event = new PresenceEvent(username, User.Status.OFFLINE, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/presence", event);
        log.info("User disconnected: {}", username);
    }

    public void userAway(String username) {
        userService.updateStatus(username, User.Status.AWAY);

        PresenceEvent event = new PresenceEvent(username, User.Status.AWAY, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/presence", event);
        log.info("User away: {}", username);
    }

    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    public boolean isUserOnline(String username) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, username));
    }
}
