package com.pa.server.Controllers;

import com.pa.server.Models.Music;
import com.pa.server.Models.Similarity;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.Repositories.SimilarityRepository;
import com.pa.server.Services.FileStorageService;
import com.pa.server.exception.MyFileNotFoundException;
import com.pa.server.exception.ResourceNotFoundException;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/music")
public class MusicController {

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private SimilarityRepository similarityRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("")
    public ResponseEntity getMusics() {
        ArrayList<Music> musicList = new ArrayList<Music>(musicRepository.findAll());
        JSONObject response = new JSONObject();
        response.put("musicList", musicList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{artistId}")
    public List<Music> getMusicsByArtistId(@PathVariable long artistId) {
        return musicRepository.findByArtistId(artistId);
    }

    @GetMapping("/non-analysed")
    public List<Music> getNonAnalysedMusics() {
        return musicRepository.findByIsAnalysedFalse();
    }

    @PostMapping("/{artistId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public Music addMusic(@PathVariable long artistId, @Valid @RequestBody Music music) {
        return artistRepository.findById(artistId)
                .map(artist -> {
                    music.setArtist(artist);
                    return musicRepository.save(music);
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with id " + artistId));
    }

    @PostMapping("/similarity/{fileName1}/{fileName2}")
    public ResponseEntity addSimilarity(@PathVariable String fileName1, @PathVariable String fileName2,
                                        @RequestBody Similarity similarity) {
        Music music1 = musicRepository.findByFileName(fileName1 + ".mp3");
        Music music2 = musicRepository.findByFileName(fileName2 + ".mp3");
        Similarity fullSimilarity = similarityRepository.save(similarity);
        music1.setSimilarity(fullSimilarity);
        music1.setAnalysed(true);
        music2.setSimilarity(fullSimilarity);
        music2.setAnalysed(true);
        musicRepository.save(music1);
        musicRepository.save(music2);
        JSONObject response = new JSONObject();
        response.put("firstMusic", music1);
        response.put("secondMusic", music2);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{musicId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public Music updateMusic(@PathVariable Long musicId, @Valid @RequestBody Music music) {
        return musicRepository.findById(musicId)
                .map(musicFound -> {
                    musicFound.setTitle(music.getTitle());
                    return musicRepository.save(musicFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }

    @PutMapping("/analysed/{musicId}")
    public Music setMusicToAnalysed(@PathVariable Long musicId) {
        return musicRepository.findById(musicId)
                .map(musicFound -> {
                    musicFound.setAnalysed(true);
                    return musicRepository.save(musicFound);
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }

    @DeleteMapping("/{musicId}")
    @PreAuthorize("hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteMusic(@PathVariable Long musicId) {
        AtomicBoolean fileRemoveRes= new AtomicBoolean(false);
        return musicRepository.findById(musicId)
                .map(music -> {
                    try {
                        fileRemoveRes.set(fileStorageService.removeFile(music.getFileName()));
                    } catch (MyFileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(fileRemoveRes.equals( true)){
                        musicRepository.delete(music);
                        return ResponseEntity.ok().build();
                    }else{
                        return ResponseEntity.notFound().build();
                    }
                }).orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));
    }
}
