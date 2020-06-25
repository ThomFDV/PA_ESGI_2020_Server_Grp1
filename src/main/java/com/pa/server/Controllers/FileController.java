package com.pa.server.Controllers;

import com.pa.server.Models.Music;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.Services.FileStorageService;
import com.pa.server.exception.MyFileNotFoundException;
import com.pa.server.exception.ResourceNotFoundException;
import com.pa.server.payload.UploadFileResponse;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private MusicRepository musicRepository;

    @PostMapping("/upload")
    public UploadFileResponse uploadFile(@RequestParam("audio") MultipartFile audio, @RequestParam String title,
                                         @RequestParam String artistName) {
        String fileName = fileStorageService.storeFile(audio);

        artistRepository.findByName(artistName)
                .map(artist -> {
                    Music music = new Music();
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setFileName(fileName);
                    return musicRepository.save(music);
                }).orElseThrow(() -> new ResourceNotFoundException("Artist not found with name " + artistName));

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/file/download/")
                .path(fileName)
                .toUriString();
        return new UploadFileResponse(fileName, fileDownloadUri, audio.getContentType(), audio.getSize());
    }

    @GetMapping("/download/{musicId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable long musicId) throws MyFileNotFoundException {

        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + musicId));

        Resource resource = fileStorageService.loadFileAsResource(music.getFileName());

        String contentType = "audio/mpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping
    public ResponseEntity listFiles(Model model) {
        model.addAttribute("files link", fileStorageService.loadAll().map(
                path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/download/")
                        .path(path.getFileName().toString())
                        .toUriString())
                .collect(Collectors.toList()));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/compare")
    public ResponseEntity readFile(@RequestParam long firstMusicId, @RequestParam long secondMusicId) throws IOException {
        Music musicOne = musicRepository.findById(firstMusicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + firstMusicId));
        Music musicTwo = musicRepository.findById(secondMusicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found with id " + secondMusicId));
        String firstFileName = musicOne.getFileName();
        String secondFileName = musicTwo.getFileName();
        boolean result = fileStorageService.areFilesEquals(firstFileName, secondFileName);
        JSONObject response = new JSONObject();
        response.put("areEquals", result);
        return ResponseEntity.ok(response);
    }
}
