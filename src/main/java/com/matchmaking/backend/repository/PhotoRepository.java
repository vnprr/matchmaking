package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<UserPhoto, Long> {
    List<UserPhoto> findByUserId(Long userId);
}
