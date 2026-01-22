package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.RefreshTokenMapper;
import com.lofi.lofiapps.model.entity.RefreshToken;
import com.lofi.lofiapps.repository.JpaRefreshTokenRepository;
import com.lofi.lofiapps.repository.RefreshTokenRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {
  private final JpaRefreshTokenRepository jpaRepository;
  private final RefreshTokenMapper mapper;

  @Override
  @Transactional
  public RefreshToken save(RefreshToken refreshToken) {
    return mapper.toDomain(jpaRepository.save(mapper.toJpa(refreshToken)));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RefreshToken> findByToken(String token) {
    return jpaRepository.findByToken(token).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RefreshToken> findByUserId(UUID userId) {
    return jpaRepository.findByUserId(userId).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void deleteByUserId(UUID userId) {
    jpaRepository.deleteByUserId(userId);
  }
}
