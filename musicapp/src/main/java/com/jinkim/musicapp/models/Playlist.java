package com.jinkim.musicapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "playlists")
@Data                   // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor      // Generates a no-args constructor
@AllArgsConstructor     // Generates a constructor with all fields
public class Playlist {
    @Id
    private String id;
    private String userId;      // Spotify ID of the user
    private String name;
    private String description;
    private List<String> trackIds;  // Spotify track IDs
}
