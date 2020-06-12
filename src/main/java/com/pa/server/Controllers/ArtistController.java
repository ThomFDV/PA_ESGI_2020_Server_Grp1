package com.pa.server.Controllers;

import com.pa.server.Models.Artist;
import com.pa.server.Models.Music;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/artist")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @GetMapping("")
    public Page<Artist> getArtists(Pageable pageable) {
        return artistRepository.findAll(pageable);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<Artist> createArtist(@Valid @RequestBody Artist artist) {
        artistRepository.save(artist);
        return ResponseEntity.ok(artist);
    }

    @PutMapping("/{artistId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public Artist updateArtist(@PathVariable Long artistId, @Valid @RequestBody Artist artist) {
        return artistRepository.findById(artistId)
                .map(artistFound -> {
                    artistFound.setName(artist.getName());
                    return artistRepository.save(artistFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with id " + artistId));
    }

    @DeleteMapping("/{artistId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteArtist(@PathVariable Long artistId) {
        return artistRepository.findById(artistId)
                .map(artist -> {
                    artistRepository.delete(artist);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with id " + artistId));
    }
}
