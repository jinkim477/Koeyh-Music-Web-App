package com.jinkim.musicapp.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinkim.musicapp.models.User;
import com.jinkim.musicapp.repositories.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<User> getUserBySpotifyId(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId);
    }

    // Find user by access token
    public Optional<User> getUserByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken);
    }

    public User createOrUpdateUser(User user) {
        return userRepository.save(user);
    }
}
