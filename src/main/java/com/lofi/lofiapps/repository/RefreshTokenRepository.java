package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
  RefreshToken save(RefreshToken refreshToken);

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}
