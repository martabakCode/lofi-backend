package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);

  @Query("SELECT r FROM RefreshToken r WHERE r.user.id = :userId")
  Optional<RefreshToken> findByUserId(UUID userId);

  @Modifying
  @Query(value = "DELETE FROM refresh_tokens WHERE user_id = :userId", nativeQuery = true)
  void deleteByUserId(@Param("userId") UUID userId);
}
