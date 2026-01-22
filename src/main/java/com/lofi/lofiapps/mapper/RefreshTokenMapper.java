package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaRefreshToken;
import com.lofi.lofiapps.model.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenMapper {
  private final UserMapper userMapper;

  public RefreshToken toDomain(JpaRefreshToken entity) {
    if (entity == null) return null;
    return RefreshToken.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .user(userMapper.toDomain(entity.getUser()))
        .token(entity.getToken())
        .expiryDate(entity.getExpiryDate())
        .revoked(entity.isRevoked())
        .build();
  }

  public JpaRefreshToken toJpa(RefreshToken domain) {
    if (domain == null) return null;
    return JpaRefreshToken.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .user(userMapper.toJpa(domain.getUser()))
        .token(domain.getToken())
        .expiryDate(domain.getExpiryDate())
        .revoked(domain.isRevoked())
        .build();
  }
}
