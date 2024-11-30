package com.dailycodework.dream_shops.repository;

import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MagacinRepository extends JpaRepository<Magacin, Long> {
   Optional<Magacin> findByUserId(Long userId);

   boolean existsById(@NotNull Long id);

   Optional<Magacin> findByIdAndUser(Long magacinId, User user);
}
