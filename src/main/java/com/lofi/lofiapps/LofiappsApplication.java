package com.lofi.lofiapps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LofiappsApplication {

  public static void main(String[] args) {
    SpringApplication.run(LofiappsApplication.class, args);
  }
}
