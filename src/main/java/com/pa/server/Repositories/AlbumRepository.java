package com.pa.server.Repositories;

import com.pa.server.Models.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtistId(long artistId);
    Optional<Album> findByName(Optional<String> albumName);
}
