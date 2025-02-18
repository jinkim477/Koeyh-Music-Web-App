package com.jinkim.musicapp.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jinkim.musicapp.models.User;

public interface UserRepository extends MongoRepository<User, String>{
    // Built in CRUD methods take care of:
    // 1. save - create or update a user
    // 2. findById - find a user by mongodb objectid
    // 3. findAll - get all users
    // 4. deleteById - delete a user by mongodb objectid

    Optional<User> findBySpotifyId(String spotifyId); // find a user by their spotify id\

    // might add findByName(String name)
}
