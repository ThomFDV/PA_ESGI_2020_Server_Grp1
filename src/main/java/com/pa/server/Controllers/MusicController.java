package com.pa.server.Controllers;

import com.pa.server.Models.Music;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/music")
public class MusicController {

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @GetMapping("")
    public Page<Music> getMusics(Pageable pageable) {
        return musicRepository.findAll(pageable);
    }

    @GetMapping("/{artistId}")
    public List<Music> getMusicsByArtistId(@PathVariable long artistId) {
        return musicRepository.findByArtistId(artistId);
    }

    @PostMapping("")
    public ResponseEntity<Music> createMusic(@Valid @RequestBody Music music) {
        musicRepository.save(music);
        return ResponseEntity.ok(music);
    }

    @PostMapping("/{artistId}")
    public Music addMusic(@PathVariable long artistId, @Valid @RequestBody Music music) {
        return artistRepository.findById(artistId)
                .map(artist -> {
                    music.setArtist(artist);
                    return musicRepository.save(music);
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with id " + artistId));
    }

    @PutMapping("/{musicId}")
    public Music updateMusic(@PathVariable Long musicId, @Valid @RequestBody Music music) {
        return musicRepository.findById(musicId)
                .map(musicFound -> {
                    musicFound.setTitle(music.getTitle());
                    return musicRepository.save(musicFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }

    @DeleteMapping("/{musicId}")
    public ResponseEntity<?> deleteMusic(@PathVariable Long musicId) {
        return musicRepository.findById(musicId)
                .map(music -> {
                    musicRepository.delete(music);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }
}
