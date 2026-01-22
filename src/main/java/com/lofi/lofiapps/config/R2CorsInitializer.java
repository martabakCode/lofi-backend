package com.lofi.lofiapps.config;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class R2CorsInitializer implements CommandLineRunner {

  private final S3Client s3Client;

  @Value("${cloudflare.r2.bucket-name}")
  private String bucketName;

  @Override
  public void run(String... args) {
    log.info("Checking/Updating R2 CORS configuration for bucket: {}", bucketName);
    try {
      CORSRule corsRule =
          CORSRule.builder()
              .allowedHeaders("*")
              .allowedMethods("GET", "PUT", "POST", "DELETE", "HEAD")
              .allowedOrigins("*")
              .maxAgeSeconds(3000)
              .build();

      CORSConfiguration corsConfiguration =
          CORSConfiguration.builder().corsRules(Collections.singletonList(corsRule)).build();

      PutBucketCorsRequest putBucketCorsRequest =
          PutBucketCorsRequest.builder()
              .bucket(bucketName)
              .corsConfiguration(corsConfiguration)
              .build();

      s3Client.putBucketCors(putBucketCorsRequest);
      log.info("Successfully set CORS configuration for bucket: {}", bucketName);

    } catch (Exception e) {
      log.error("Failed to set CORS configuration for bucket: {}", bucketName, e);
    }
  }
}
