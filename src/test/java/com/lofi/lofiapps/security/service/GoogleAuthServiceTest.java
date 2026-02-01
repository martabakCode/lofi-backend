package com.lofi.lofiapps.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

  @Mock private FirebaseAuth firebaseAuth;

  @InjectMocks private GoogleAuthService googleAuthService;

  @Test
  void verifyGoogleToken_ValidToken_ShouldReturnGoogleUser() throws FirebaseAuthException {
    String tokenString = "validToken";
    FirebaseToken firebaseToken = mock(FirebaseToken.class);

    when(firebaseToken.getEmail()).thenReturn("test@example.com");
    when(firebaseToken.getName()).thenReturn("Test User");
    when(firebaseToken.getPicture()).thenReturn("http://pic.url");
    when(firebaseToken.getUid()).thenReturn("uid123");

    when(firebaseAuth.verifyIdToken(tokenString)).thenReturn(firebaseToken);

    GoogleUser result = googleAuthService.verifyGoogleToken(tokenString);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals("Test User", result.getName());
    assertEquals("http://pic.url", result.getPicture());
    assertEquals("uid123", result.getUid());

    verify(firebaseAuth).verifyIdToken(tokenString);
  }

  @Test
  void verifyGoogleToken_InvalidToken_ShouldReturnNull() throws FirebaseAuthException {
    String tokenString = "invalidToken";
    when(firebaseAuth.verifyIdToken(tokenString)).thenThrow(new RuntimeException("Invalid token"));

    GoogleUser result = googleAuthService.verifyGoogleToken(tokenString);

    assertNull(result);
    verify(firebaseAuth).verifyIdToken(tokenString);
  }
}
