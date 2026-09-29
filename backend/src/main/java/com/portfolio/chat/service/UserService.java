package com.portfolio.chat.service;

import com.portfolio.chat.entity.User;
import com.portfolio.chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateStatus(String username, User.Status status) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(status);
            userRepository.save(user);
            log.debug("Updated status for user {}: {}", username, status);
        });
    }
}
