package com.lofi.lofiapps.service.impl.mapper;

import com.lofi.lofiapps.dto.response.DocumentResponse;
import com.lofi.lofiapps.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

  public DocumentResponse toResponse(Document document) {
    if (document == null) {
      return null;
    }
    return DocumentResponse.builder()
        .id(document.getId())
        .fileName(document.getFileName())
        .documentType(document.getDocumentType())
        .uploadedAt(document.getCreatedAt())
        .build();
  }
}
