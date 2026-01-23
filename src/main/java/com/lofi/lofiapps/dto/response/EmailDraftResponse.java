package com.lofi.lofiapps.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDraftResponse {
  private String subject;
  private String bodyHtml;
  private String disclaimer;
}
