package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaUser;
import com.lofi.lofiapps.model.entity.JpaUserBiodata;
import com.lofi.lofiapps.model.entity.User;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
  private final BranchMapper branchMapper;
  private final ProductMapper productMapper;
  private final UserBiodataMapper userBiodataMapper;
  private final RoleMapper roleMapper;

  public User toDomain(JpaUser entity) {
    if (entity == null) return null;
    return User.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .branch(branchMapper.toDomain(entity.getBranch()))
        .username(entity.getUsername())
        .fullName(entity.getFullName())
        .email(entity.getEmail())
        .password(entity.getPassword())
        .phoneNumber(entity.getPhoneNumber())
        .firebaseToken(entity.getFirebaseToken())
        .status(entity.getStatus())
        .product(productMapper.toDomain(entity.getProduct()))
        .loansCompleted(entity.getLoansCompleted())
        .totalOverdueDays(entity.getTotalOverdueDays())
        .profileCompleted(entity.getProfileCompleted())
        .userBiodata(userBiodataMapper.toDomain(entity.getUserBiodata()))
        .roles(
            entity.getRoles() != null
                ? entity.getRoles().stream().map(roleMapper::toDomain).collect(Collectors.toSet())
                : null)
        .build();
  }

  public JpaUser toJpa(User domain) {
    if (domain == null) return null;
    JpaUser jpaUser =
        JpaUser.builder()
            .id(domain.getId())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .lastModifiedBy(domain.getLastModifiedBy())
            .deletedAt(domain.getDeletedAt())
            .branch(branchMapper.toJpa(domain.getBranch()))
            .username(domain.getUsername())
            .fullName(domain.getFullName())
            .email(domain.getEmail())
            .password(domain.getPassword())
            .phoneNumber(domain.getPhoneNumber())
            .firebaseToken(domain.getFirebaseToken())
            .status(domain.getStatus())
            .product(productMapper.toJpa(domain.getProduct()))
            .loansCompleted(domain.getLoansCompleted())
            .totalOverdueDays(domain.getTotalOverdueDays())
            .profileCompleted(domain.getProfileCompleted())
            .roles(
                domain.getRoles() != null
                    ? domain.getRoles().stream().map(roleMapper::toJpa).collect(Collectors.toSet())
                    : null)
            .build();

    if (domain.getUserBiodata() != null) {
      JpaUserBiodata biodata = userBiodataMapper.toJpa(domain.getUserBiodata());
      biodata.setUser(jpaUser);
      jpaUser.setUserBiodata(biodata);
    }
    return jpaUser;
  }
}
