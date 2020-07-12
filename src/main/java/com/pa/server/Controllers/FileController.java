package com.pa.server.Controllers;

import com.pa.server.Models.Album;
import com.pa.server.Models.Artist;
import com.pa.server.Models.Music;
import com.pa.server.Repositories.AlbumRepository;
import com.pa.server.Repositories.ArtistRepository;
import com.pa.server.Repositories.MusicRepository;
import com.pa.server.Services.FileStorageService;
import com.pa.server.exception.MyFileNotFoundException;
import com.pa.server.exception.ResourceNotFoundException;
import com.pa.server.payload.UploadFileResponse;
import net.minidev.json.JSONObject;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
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
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
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

    @Autowired
    private AlbumRepository albumRepository;

    @PostMapping("/upload")
    public UploadFileResponse uploadFile(@RequestParam("audio") MultipartFile audio)
                                        throws TikaException, IOException, SAXException {
        String fileName = fileStorageService.storeFile(audio);
        Metadata fileMetadata = fileStorageService.getMetadata(fileName);
        String artistName = fileMetadata.get("xmpDM:artist");
        String title = fileMetadata.get("title");
        String albumName = fileMetadata.get("xmpDM:album");
        Artist artist = artistRepository.findByName(artistName).orElse(null);
        if (artist == null) {
            artist = artistRepository.save(new Artist(artistName));
        }
        Music music = new Music();
        music.setTitle(title);
        music.setArtist(artist);
        music.setFileName(fileName);
        music.setAnalysed(false);
        music.setAlbum(albumRepository.findByName(Optional.ofNullable(albumName)).orElse(null));
        musicRepository.save(music);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/file/download/")
                .path(fileName)
                .toUriString();
        return new UploadFileResponse(fileName, fileDownloadUri, audio.getContentType(), audio.getSize());
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws MyFileNotFoundException {

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = "audio/mpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/read/{fileName}")
    public ResponseEntity<Resource> readFile(@PathVariable String fileName) throws MyFileNotFoundException {

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = "audio/mpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping
    public ResponseEntity listFiles(Model model) {
        model.addAttribute("files link", fileStorageService.loadAll().map(
                path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/file/download/")
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
