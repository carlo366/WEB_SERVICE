package com.example.web_service.controller;

import com.example.web_service.dto.Response;
import com.example.web_service.entity.User;
import com.example.web_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired 
    private UserService userService;

    // === Get user by username ===
    @GetMapping("/username/{username}")
    public Response<Map<String, Object>> getProfileByUsername(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username);

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("avatar", user.getAvatar());
            data.put("bio", user.getBio());
            data.put("followers", userService.countFollowers(user.getId()));
            data.put("following", userService.countFollowing(user.getId()));

            return Response.successfulResponse("User profile found!", data);

        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public Response<Map<String, Object>> getProfileById(@PathVariable String id) {
        try {
            UUID userId = UUID.fromString(id);
            User user = userService.findById(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("avatar", user.getAvatar());
            data.put("bio", user.getBio());
            data.put("followers", userService.countFollowers(user.getId()));
            data.put("following", userService.countFollowing(user.getId()));

            return Response.successfulResponse("User profile found!", data);

        } catch (Exception e) {
            return Response.failedResponse(e.getMessage());
        }
    }
}
