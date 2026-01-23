package com.lofi.lofiapps.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentValidationResponse {
  private Double confidence;
  private List<String> issues;
  private List<String> recommendations;
}
