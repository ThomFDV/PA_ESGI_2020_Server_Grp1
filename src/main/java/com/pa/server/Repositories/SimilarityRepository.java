package com.pa.server.Repositories;

import com.pa.server.Models.Role;
import com.pa.server.Models.Similarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    Optional<Role> findByValue(Float val);
}
