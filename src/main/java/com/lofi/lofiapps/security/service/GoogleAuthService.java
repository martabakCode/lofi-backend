package com.lofi.lofiapps.security.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {

  private final FirebaseAuth firebaseAuth;

  /**
   * Verifies the Google ID token (Firebase ID token) using Firebase Admin SDK.
   *
   * @param idTokenString The ID token sent from the client.
   * @return A GoogleUser object containing user details, or null if verification fails.
   */
  public GoogleUser verifyGoogleToken(String idTokenString) {
    try {
      FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idTokenString);
      return GoogleUser.builder()
          .email(decodedToken.getEmail())
          .name(decodedToken.getName())
          .picture(decodedToken.getPicture())
          .uid(decodedToken.getUid())
          .build();
    } catch (Exception e) {
      log.error("Firebase Token Verification Failed: {}", e.getMessage());
      return null;
    }
  }
}
