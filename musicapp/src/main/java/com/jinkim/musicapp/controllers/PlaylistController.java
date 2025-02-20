package com.jinkim.musicapp.controllers;

import com.jinkim.musicapp.models.Playlist;
import com.jinkim.musicapp.services.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    // Create a new playlist
    @PostMapping("/create")
    public ResponseEntity<Playlist> createPlaylist(@RequestBody Playlist playlist) {
        Playlist savedPlaylist = playlistService.savePlaylist(playlist);
        return ResponseEntity.ok(savedPlaylist);
    }

    // Get all playlists for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Playlist>> getUserPlaylists(@PathVariable String userId) {
        List<Playlist> playlists = playlistService.getPlaylistsByUserId(userId);
    
        // Return an empty array if no playlists are found
        if (playlists.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    
        return ResponseEntity.ok(playlists);
    }
    

    // Get a specific playlist by ID
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable String id) {
        Optional<Playlist> playlist = playlistService.getPlaylistById(id);
        return playlist.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a playlist
    @PutMapping("/update/{id}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable String id, @RequestBody Playlist updatedPlaylist) {
        Optional<Playlist> existingPlaylist = playlistService.getPlaylistById(id);
        if (existingPlaylist.isPresent()) {
            Playlist playlist = existingPlaylist.get();
            playlist.setName(updatedPlaylist.getName());
            playlist.setDescription(updatedPlaylist.getDescription());
            playlist.setTrackIds(updatedPlaylist.getTrackIds());
            playlistService.savePlaylist(playlist);
            return ResponseEntity.ok(playlist);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a playlist
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable String id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}
