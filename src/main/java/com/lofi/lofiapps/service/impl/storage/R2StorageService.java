package com.lofi.lofiapps.service.impl.storage;

import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

  private final S3Presigner s3Presigner;

  @Override
  public URL generatePresignedUploadUrl(
      String bucketName, String objectKey, String contentType, long expirationMinutes) {
    try {
      PutObjectRequest objectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(objectKey)
              .contentType(contentType) // Add Content-Type to signature
              .build();

      PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(expirationMinutes))
              .putObjectRequest(objectRequest)
              .build();

      return s3Presigner.presignPutObject(presignRequest).url();
    } catch (Exception e) {
      log.error("Error generating presigned upload URL", e);
      throw new RuntimeException("Error generating presigned upload URL", e);
    }
  }

  @Override
  public URL generatePresignedDownloadUrl(
      String bucketName, String objectKey, long expirationMinutes) {
    try {
      GetObjectRequest objectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(expirationMinutes))
              .getObjectRequest(objectRequest)
              .build();

      return s3Presigner.presignGetObject(presignRequest).url();
    } catch (Exception e) {
      log.error("Error generating presigned download URL", e);
      throw new RuntimeException("Error generating presigned download URL", e);
    }
  }
}
