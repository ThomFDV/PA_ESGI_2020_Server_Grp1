package com.pa.server.Repositories;

import com.pa.server.Models.Playlist;
import com.pa.server.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(long userId);
    Optional<Playlist> findByName(String name);
}
