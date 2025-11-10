package com.example.web_service.service;

import com.example.web_service.entity.User;
import com.example.web_service.exception.ApplicationException;
import com.example.web_service.repository.FollowRepository;
import com.example.web_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,"User '%s' not found!".formatted(username)));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ApplicationException(HttpStatus.CONFLICT,"Username already taken!");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        return userRepository.save(user);
    }

    // ✅ Update profil
    public User saveProfile(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,"User tidak ditemukan"));

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setAvatar(user.getAvatar());
        existing.setBio(user.getBio());

        return userRepository.save(existing);
    }

    public long countFollowers(UUID userId) {
        return followRepository.countByFolloweeId(userId);
    }

    public long countFollowing(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }
}
