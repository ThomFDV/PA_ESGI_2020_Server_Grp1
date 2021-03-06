package com.pa.server.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "musics")
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    Album album;

    private String fileName;

    private boolean isAnalysed;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "music_similarities",
            joinColumns = @JoinColumn(name = "music_id"),
            inverseJoinColumns = @JoinColumn(name = "similarity_id"))
    private Set<Similarity> similarity = new HashSet<>();

    public Music() {
        this.isAnalysed = false;
    }

    public Music(Long id, String title, Artist artist, Album album, String fileName) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.fileName = fileName;
        this.isAnalysed = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isAnalysed() {
        return isAnalysed;
    }

    public void setAnalysed(boolean analysed) {
        isAnalysed = analysed;
    }

    public Set<Similarity> getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity.add(similarity);
    }
}
