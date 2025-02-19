package com.jinkim.musicapp.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinkim.musicapp.models.User;
import com.jinkim.musicapp.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/{spotifyId}") // get user by spotify id
    public Optional<User> getUserBySpotifyId(@PathVariable String spotifyId) {
        return userService.getUserBySpotifyId(spotifyId);
    }

    @PostMapping
    public User createOrUpdateUser(@RequestBody User user) {
        return userService.createOrUpdateUser(user);
    }
}
