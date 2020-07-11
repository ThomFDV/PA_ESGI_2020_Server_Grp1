package com.pa.server.Repositories;

import com.pa.server.Models.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findByArtistId(long artistId);
    Optional<Music> findByTitle(String title);
    List<Music> findByIsAnalysedFalse();
    Music findByFileName(String fileName);
}
