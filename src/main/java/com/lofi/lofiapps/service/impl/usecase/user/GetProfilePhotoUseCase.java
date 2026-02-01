package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.StorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProfilePhotoUseCase {

  private final UserRepository userRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  public byte[] execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    String photoKey = user.getProfilePictureUrl();
    if (photoKey == null || photoKey.isEmpty()) {
      throw new ResourceNotFoundException("Profile picture not found for user: " + userId);
    }

    // If it's a full URL (legacy), we might not be able to download it this way
    // but assuming we save keys now.
    if (photoKey.startsWith("http")) {
      // Ideally handled differently, but for now we expect keys
      throw new RuntimeException(
          "Profile picture is stored as a remote URL and cannot be downloaded directly");
    }

    return storageService.downloadFile(bucketName, photoKey);
  }
}
