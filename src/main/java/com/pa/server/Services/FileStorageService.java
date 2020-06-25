package com.pa.server.Services;

import com.pa.server.exception.FileStorageException;
import com.pa.server.exception.MyFileNotFoundException;
import com.pa.server.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! File name contains invalid path sequence " + fileName);
            }
            if(!Objects.equals(file.getContentType(), "audio/mpeg")) {
                throw new FileStorageException("Sorry! File type is not authorized. Please try with MP3");
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.fileStorageLocation, 1)
                    .filter(path -> !path.equals(this.fileStorageLocation))
                    .map(this.fileStorageLocation::relativize);
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to read stored files", e);
        }

    }

    public Resource loadFileAsResource(String fileName) throws MyFileNotFoundException {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException | MyFileNotFoundException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    public String readBytes(String fileName) throws IOException {
        Path path = this.fileStorageLocation.resolve(fileName).normalize();
        byte[] encoded = Files.readAllBytes(path);
        return  new String(encoded, Charset.forName("UTF-8"));
    }

    public boolean areFilesEquals(String firstFile, String secondFile) throws IOException {
        String firstFileName = firstFile + ".mp3";
        String secondFileName = secondFile + ".mp3";
        String firstFileBytes = readBytes(firstFileName);
        String secondFileBytes = readBytes(secondFileName);
        return firstFileBytes.equals(secondFileBytes);
    }
}
