package com.lofi.lofiapps.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
public class FirebaseConfig {

  @Bean
  @Primary
  public FirebaseApp firebaseApp() throws IOException {
    // 1. Check if ANY app is already initialized and return it to prevent "already
    // exists" error
    List<FirebaseApp> apps = FirebaseApp.getApps();
    if (apps != null && !apps.isEmpty()) {
      for (FirebaseApp app : apps) {
        if (FirebaseApp.DEFAULT_APP_NAME.equals(app.getName())) {
          return app;
        }
      }
      // If default app not found but others exist, return first one or create
      // default?
      // Usually only one app.
      return apps.get(0);
    }

    // 2. Initialize new app
    ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
    if (!resource.exists()) {
      log.error(
          "Firebase Service Account file NOT FOUND at classpath:firebase-service-account.json");
      // Throwing exception here stops context startup, which is good if Firebase is
      // required.
      throw new IOException("Firebase service account file not found");
    }

    try (InputStream serviceAccount = resource.getInputStream()) {
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      return FirebaseApp.initializeApp(options);
    } catch (IOException e) {
      log.error("Failed to initialize FirebaseApp: {}", e.getMessage());
      throw e;
    }
  }

  @Bean
  @Primary
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
