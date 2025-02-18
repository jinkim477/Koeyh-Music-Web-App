package com.jinkim.musicapp.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users") // this tells mongodb to store this in the "users" collection
public class User {
    @Id
    private String id; // mongodb automatically generates an ObjectId

    private String spotifyId;
    private String name;
    private String email;
    private String profilePicture;
    private List<String> savedAlbums;
    private List<String> playlists;
}
