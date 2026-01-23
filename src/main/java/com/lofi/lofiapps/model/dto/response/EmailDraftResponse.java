package com.lofi.lofiapps.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDraftResponse {
  private String subject;
  private String bodyHtml;
  private String disclaimer;
}
