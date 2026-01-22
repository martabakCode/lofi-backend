package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.Branch;
import com.lofi.lofiapps.model.entity.JpaBranch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {
  public Branch toDomain(JpaBranch entity) {
    if (entity == null) return null;
    return Branch.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .name(entity.getName())
        .address(entity.getAddress())
        .city(entity.getCity())
        .state(entity.getState())
        .zipCode(entity.getZipCode())
        .phone(entity.getPhone())
        .longitude(entity.getLongitude())
        .latitude(entity.getLatitude())
        .build();
  }

  public JpaBranch toJpa(Branch domain) {
    if (domain == null) return null;
    return JpaBranch.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .name(domain.getName())
        .address(domain.getAddress())
        .city(domain.getCity())
        .state(domain.getState())
        .zipCode(domain.getZipCode())
        .phone(domain.getPhone())
        .longitude(domain.getLongitude())
        .latitude(domain.getLatitude())
        .build();
  }
}
