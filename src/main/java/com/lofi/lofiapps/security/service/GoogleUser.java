package com.lofi.lofiapps.security.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoogleUser {
  private String email;
  private String name;
  private String picture;
  private String uid;
}
