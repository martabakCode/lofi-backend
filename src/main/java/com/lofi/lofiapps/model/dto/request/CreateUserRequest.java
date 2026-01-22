package com.lofi.lofiapps.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  private UUID branchId;
}
