package com.lofi.lofiapps.model.dto.response;

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
  private String longitude;
  private String latitude;
}
