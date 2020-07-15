package com.pa.server.Controllers;

import com.pa.server.DAO.PlaylistMusicDAO;
import com.pa.server.Models.Music;
import com.pa.server.Models.Playlist;
import com.pa.server.Models.User;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.Repositories.PlaylistRepository;
import com.pa.server.Repositories.UserRepository;
import com.pa.server.exception.ResourceNotFoundException;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MusicRepository musicRepository;

    @GetMapping
    public ResponseEntity getPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<>(playlistRepository.findAll());
        JSONObject response = new JSONObject();
        response.put("playlistList", playlists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity getConnectedUserPlaylists() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        ArrayList<Playlist> playlists = new ArrayList<>(playlistRepository.findByUserId(user.getId()));
        JSONObject response = new JSONObject();
        response.put("playlistList", playlists);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Playlist addPlaylist(@Valid @RequestBody Playlist playlist) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    playlist.setUser(user);
                    return playlistRepository.save(playlist);
                }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userDetails.getUsername()));
    }

    @PostMapping("/music")
    public Playlist addMusicToPlaylist(@RequestBody PlaylistMusicDAO playlistMusicDAO) {
        Music music = musicRepository.findByTitle(playlistMusicDAO.getMusicTitle())
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with Title " + playlistMusicDAO.getMusicTitle()));
        return playlistRepository.findByName(playlistMusicDAO.getPlaylistName())
                .map(playlist -> {
                    playlist.addMusic(music.getFileName());
                    return playlistRepository.save(playlist);
                }).orElseThrow(() -> new ResourceNotFoundException("Playlist not found with id " + playlistMusicDAO.getPlaylistName()));
    }

    @PostMapping("/multiple/{playlistId}")
    public Playlist addMultipleMusicsToPlaylist(@PathVariable long playlistId, @RequestParam List<Long> musicsIds) {
        return playlistRepository.findById(playlistId)
                .map(playlist -> {
                    for (Long musicId : musicsIds) {
                        Music music = musicRepository.findById(musicId)
                                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
                        playlist.addMusic(music.getFileName());
                    }
                    return playlistRepository.save(playlist);
                }).orElseThrow(() -> new ResourceNotFoundException("Playlist not found with id " + playlistId));
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
