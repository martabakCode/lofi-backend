package com.lofi.lofiapps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSourceResponse {
  private String authSource; // "GOOGLE" or "TRADITIONAL"
  private boolean isGoogleUser;
}
