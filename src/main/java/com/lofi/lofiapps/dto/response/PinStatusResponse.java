package com.lofi.lofiapps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinStatusResponse {
  /** Indicates whether the user has a PIN set. */
  private boolean pinSet;
}
