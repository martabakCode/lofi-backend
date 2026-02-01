package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.response.UserProfileResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.StorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UpdateProfilePictureUseCase {

  private final UserRepository userRepository;
  private final GetUserProfileUseCase getUserProfileUseCase;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  @Transactional
  public UserProfileResponse execute(UUID userId, MultipartFile photo) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    if (photo != null && !photo.isEmpty()) {
      String originalFilename = photo.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      } else {
        // Fallback to content type mapping if original filename has no extension
        String contentType = photo.getContentType();
        if ("image/jpeg".equals(contentType)) extension = ".jpg";
        else if ("image/png".equals(contentType)) extension = ".png";
        else if ("image/gif".equals(contentType)) extension = ".gif";
      }

      String fileName = "profile-pictures/" + userId + "-" + System.currentTimeMillis() + extension;
      try {
        storageService.uploadFile(bucketName, fileName, photo.getBytes(), photo.getContentType());
        user.setProfilePictureUrl(fileName);
      } catch (java.io.IOException e) {

        throw new RuntimeException("Failed to upload profile picture", e);
      }
    } else {
      throw new IllegalArgumentException("Photo file must not be empty");
    }

    user = userRepository.save(user);

    return getUserProfileUseCase.mapToProfileResponse(user);
  }
}
