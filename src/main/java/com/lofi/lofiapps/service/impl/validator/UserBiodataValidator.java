package com.lofi.lofiapps.service.impl.validator;

import com.lofi.lofiapps.entity.UserBiodata;
import com.lofi.lofiapps.repository.UserBiodataRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBiodataValidator {

  private final UserBiodataRepository userBiodataRepository;

  public UserBiodata validateAndGet(UUID userId) {
    UserBiodata biodata =
        userBiodataRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "User biodata is incomplete. Please complete your biodata first."));
    validateComplete(biodata);
    return biodata;
  }

  public void validateComplete(UserBiodata userBiodata) {
    validateField(userBiodata.getNik(), "NIK");
    validateField(userBiodata.getDateOfBirth(), "Date of birth");
    validateField(userBiodata.getPlaceOfBirth(), "Place of birth");
    validateField(userBiodata.getAddress(), "Address");
    validateField(userBiodata.getCity(), "City");
    validateField(userBiodata.getProvince(), "Province");
    validateField(userBiodata.getMonthlyIncome(), "Monthly income");
    validateField(userBiodata.getIncomeSource(), "Income source");
    validateField(userBiodata.getOccupation(), "Occupation");
  }

  private void validateField(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("User biodata is incomplete: " + fieldName + " is required.");
    }
  }

  private void validateField(Object value, String fieldName) {
    if (value == null) {
      throw new IllegalStateException("User biodata is incomplete: " + fieldName + " is required.");
    }
  }
}
