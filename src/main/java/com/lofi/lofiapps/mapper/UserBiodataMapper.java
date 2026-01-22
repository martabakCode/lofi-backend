package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaUserBiodata;
import com.lofi.lofiapps.model.entity.UserBiodata;
import org.springframework.stereotype.Component;

@Component
public class UserBiodataMapper {

  public UserBiodata toDomain(JpaUserBiodata entity) {
    if (entity == null) return null;
    return UserBiodata.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .incomeSource(entity.getIncomeSource())
        .incomeType(entity.getIncomeType())
        .monthlyIncome(entity.getMonthlyIncome())
        .age(entity.getAge())
        .nik(entity.getNik())
        .dateOfBirth(entity.getDateOfBirth())
        .placeOfBirth(entity.getPlaceOfBirth())
        .city(entity.getCity())
        .address(entity.getAddress())
        .province(entity.getProvince())
        .district(entity.getDistrict())
        .subDistrict(entity.getSubDistrict())
        .postalCode(entity.getPostalCode())
        .phoneNumber(entity.getPhoneNumber())
        .gender(entity.getGender())
        .maritalStatus(entity.getMaritalStatus())
        .education(entity.getEducation())
        .occupation(entity.getOccupation())
        .build();
  }

  public JpaUserBiodata toJpa(UserBiodata domain) {
    if (domain == null) return null;
    return JpaUserBiodata.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .incomeSource(domain.getIncomeSource())
        .incomeType(domain.getIncomeType())
        .monthlyIncome(domain.getMonthlyIncome())
        .age(domain.getAge())
        .nik(domain.getNik())
        .dateOfBirth(domain.getDateOfBirth())
        .placeOfBirth(domain.getPlaceOfBirth())
        .city(domain.getCity())
        .address(domain.getAddress())
        .province(domain.getProvince())
        .district(domain.getDistrict())
        .subDistrict(domain.getSubDistrict())
        .postalCode(domain.getPostalCode())
        .phoneNumber(domain.getPhoneNumber())
        .gender(domain.getGender())
        .maritalStatus(domain.getMaritalStatus())
        .education(domain.getEducation())
        .occupation(domain.getOccupation())
        .build();
  }
}
