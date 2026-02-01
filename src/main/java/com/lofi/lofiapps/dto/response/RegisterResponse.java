package com.lofi.lofiapps.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
  private UUID id;
  private String fullName;
  private String username;
  private String email;
  private String phoneNumber;
  private LocalDateTime createdAt;
}
