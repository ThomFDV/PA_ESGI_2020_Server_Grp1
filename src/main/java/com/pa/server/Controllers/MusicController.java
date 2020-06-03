package com.pa.server.Controllers;

import com.pa.server.Models.Music;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/music")
public class MusicController {

    @Autowired
    private MusicRepository musicRepository;

    @GetMapping("")
    public Page<Music> getMusics(Pageable pageable) {
        return musicRepository.findAll(pageable);
    }

    @PostMapping("")
    public ResponseEntity<Music> createMusic(@Valid @RequestBody Music music) {
        musicRepository.save(music);
        return ResponseEntity.ok(music);
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
    public  ResponseEntity<?> deleteMusic(@PathVariable Long musicId) {
        return musicRepository.findById(musicId)
                .map(music -> {
                    musicRepository.delete(music);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }
}
