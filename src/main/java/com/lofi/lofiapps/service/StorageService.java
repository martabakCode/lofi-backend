package com.lofi.lofiapps.service;

import java.net.URL;

public interface StorageService {
  URL generatePresignedUploadUrl(
      String bucketName, String objectKey, String contentType, long expirationMinutes);

  URL generatePresignedDownloadUrl(String bucketName, String objectKey, long expirationMinutes);

  void uploadFile(String bucketName, String objectKey, byte[] content, String contentType);

  byte[] downloadFile(String bucketName, String objectKey);
}
