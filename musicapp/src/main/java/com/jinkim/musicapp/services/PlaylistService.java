package com.jinkim.musicapp.services;

import com.jinkim.musicapp.models.Playlist;
import com.jinkim.musicapp.repositories.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    // Create or Update a Playlist
    public Playlist savePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    // Get all playlists for a user
    public List<Playlist> getPlaylistsByUserId(String userId) {
        return playlistRepository.findByUserId(userId);
    }

    // Get a single playlist by ID
    public Optional<Playlist> getPlaylistById(String id) {
        return playlistRepository.findById(id);
    }

    // Delete a playlist by ID
    public void deletePlaylist(String id) {
        playlistRepository.deleteById(id);
    }
}
