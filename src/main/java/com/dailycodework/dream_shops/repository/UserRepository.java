package com.dailycodework.dream_shops.repository;

import com.dailycodework.dream_shops.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);

    Optional<User> findById(Long userId);

}
