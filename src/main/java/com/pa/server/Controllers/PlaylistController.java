package com.pa.server.Controllers;

import com.pa.server.Models.Playlist;
import com.pa.server.Repositories.PlaylistRepository;
import com.pa.server.Repositories.UserRepository;
import com.pa.server.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("")
    public Page<Playlist> getPlaylists(Pageable pageable) {
        return playlistRepository.findAll(pageable);
    }

    @GetMapping("/{userId}")
    public List<Playlist> getPlaylistsByUserId(@PathVariable long userId) {
        return playlistRepository.findByUserId(userId);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Playlist addPlaylist(@PathVariable long userId, @Valid @RequestBody Playlist playlist) {
        return userRepository.findById(userId)
                .map(user -> {
                    playlist.setUser(user);
                    return playlistRepository.save(playlist);
                }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    @PutMapping("/{playlistId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Playlist updatePlaylist(@PathVariable Long playlistId, @Valid @RequestBody Playlist playlist) {
        return playlistRepository.findById(playlistId)
                .map(playlistFound -> {
                    playlistFound.setName(playlist.getName());
                    return playlistRepository.save(playlistFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Playlist not found with id " + playlistId));
    }

    @DeleteMapping("/{playlistId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePlaylist(@PathVariable Long playlistId) {
        return playlistRepository.findById(playlistId)
                .map(playlist -> {
                    playlistRepository.delete(playlist);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Playlist not found with id " + playlistId));
    }

}
