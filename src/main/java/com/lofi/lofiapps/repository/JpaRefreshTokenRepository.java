package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaRefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<JpaRefreshToken, UUID> {
  Optional<JpaRefreshToken> findByToken(String token);

  @Query("SELECT r FROM JpaRefreshToken r WHERE r.user.id = :userId")
  Optional<JpaRefreshToken> findByUserId(UUID userId);

  @Modifying
  @Query(value = "DELETE FROM refresh_tokens WHERE user_id = :userId", nativeQuery = true)
  void deleteByUserId(@Param("userId") UUID userId);
}
