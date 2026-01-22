package com.lofi.lofiapps.security.jwt;

import com.lofi.lofiapps.security.service.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtils {

  @Value("${lofi.app.jwtSecret:lofiapps_secret_key_lofiapps_secret_key}")
  private String jwtSecret;

  @Value("${lofi.app.jwtExpirationMs:86400000}")
  private int jwtExpirationMs;

  @Value("${lofi.app.jwtRefreshExpirationMs:604800000}")
  private int jwtRefreshExpirationMs;

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generateJwtToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject(userPrincipal.getId().toString())
        .claim("email", userPrincipal.getEmail())
        .claim(
            "roles",
            userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList()))
        .claim(
            "permissions",
            userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toList()))
        .claim(
            "branchId",
            userPrincipal.getBranchId() != null ? userPrincipal.getBranchId().toString() : null)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject(userPrincipal.getId().toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUserIdFromJwtToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public String getEmailFromJwtToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("email", String.class);
  }

  public String getUserNameFromJwtToken(String token) {
    return getEmailFromJwtToken(token);
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
      return true;
    } catch (SecurityException e) {
      log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  public long getExpirationFromJwtToken(String token) {
    Date expiration =
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
    return expiration.getTime() - new Date().getTime();
  }

  public Date getIssuedAtFromJwtToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getIssuedAt();
  }
}
