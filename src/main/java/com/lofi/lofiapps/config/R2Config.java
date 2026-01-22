package com.lofi.lofiapps.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class R2Config {

  @Value("${cloudflare.r2.access-key-id:placeholder}")
  private String accessKeyId;

  @Value("${cloudflare.r2.secret-access-key:placeholder}")
  private String secretAccessKey;

  @Value("${cloudflare.r2.bucket-name:placeholder}")
  private String bucketName;

  @Value("${cloudflare.r2.endpoint:https://placeholder.r2.cloudflarestorage.com}")
  private String endpoint;

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .region(Region.US_EAST_1) // R2 uses US_EAST_1 equivalent
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
        .build();
  }

  @Bean
  public software.amazon.awssdk.services.s3.S3Client s3Client() {
    return software.amazon.awssdk.services.s3.S3Client.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
        .build();
  }
}
