package com.pa.server.Repositories;

import com.pa.server.Models.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findByArtistId(long artistId);
    List<Music> findByIsAnalysedFalse();
    Music findByFileName(String fileName);
}
