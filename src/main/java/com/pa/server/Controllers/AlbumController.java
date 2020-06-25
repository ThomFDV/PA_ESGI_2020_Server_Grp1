package com.pa.server.Controllers;

import com.pa.server.Models.Album;
import com.pa.server.Models.Music;
import com.pa.server.Repositories.AlbumRepository;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.Repositories.MusicRepository;
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
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @GetMapping("")
    public Page<Album> getAlbums(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    @GetMapping("/{artistId}")
    public List<Album> getAlbumsByArtistId(@PathVariable long artistId) {
        return albumRepository.findByArtistId(artistId);
    }

    @PostMapping("/{artistId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public Album addAlbum(@PathVariable long artistId, @Valid @RequestBody Album album) {
        return artistRepository.findById(artistId)
                .map(artist -> {
                    album.setArtist(artist);
                    return albumRepository.save(album);
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with id " + artistId));
    }

    @PutMapping("/{albumId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public Album updateAlbum(@PathVariable Long albumId, @Valid @RequestBody Album album) {
        return albumRepository.findById(albumId)
                .map(albumFound -> {
                    albumFound.setName(album.getName());
                    return albumRepository.save(albumFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Album not found with id " + albumId));
    }

    @DeleteMapping("/{albumId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAlbum(@PathVariable Long albumId) {
        return albumRepository.findById(albumId)
                .map(music -> {
                    albumRepository.delete(music);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Album not found with id " + albumId));
    }
}
