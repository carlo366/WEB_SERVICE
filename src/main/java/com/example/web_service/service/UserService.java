package com.example.web_service.service;

import com.example.web_service.entity.User;
import com.example.web_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    public User saveProfile(User user) {
        if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new RuntimeException("Username sudah dipakai!");
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
