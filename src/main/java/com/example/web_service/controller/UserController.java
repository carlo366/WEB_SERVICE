package com.example.web_service.controller;

import com.example.web_service.entity.User;
import com.example.web_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired private UserService userService;

    @GetMapping("/{username}")
    public Map<String,Object> getProfile(@PathVariable String username){
        Map<String,Object> resp = new HashMap<>();
        try {
            User user = userService.findByUsername(username);
            Map<String,Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("avatar", user.getAvatar());
            data.put("bio", user.getBio());
            data.put("followers", userService.countFollowers(user.getId()));
            data.put("following", userService.countFollowing(user.getId()));

            resp.put("status_code", 200);
            resp.put("message", "User profile found!");
            resp.put("success", true);
            resp.put("data", data);
        } catch (Exception e){
            resp.put("status_code", 400);
            resp.put("message", e.getMessage());
            resp.put("success", false);
            resp.put("data", null);
        }
        return resp;
    }
}
