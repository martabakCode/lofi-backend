package com.lofi.lofiapps.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequest {
  @NotBlank(message = "Branch name is required")
  private String name;

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "State is required")
  private String state;

  @NotBlank(message = "Zip code is required")
  private String zipCode;

  @NotBlank(message = "Phone is required")
  private String phone;

  private String longitude;
  private String latitude;
}
