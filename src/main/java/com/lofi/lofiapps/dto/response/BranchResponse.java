package com.lofi.lofiapps.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
  private UUID id;
  private String name;
  private String address;
  private String city;
  private String state;
  private String zipCode;
  private String phone;
  private java.math.BigDecimal longitude;
  private java.math.BigDecimal latitude;
}
