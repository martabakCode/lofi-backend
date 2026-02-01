package com.lofi.lofiapps.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lofi.lofiapps.security.service.TokenBlacklistService;
import com.lofi.lofiapps.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

  @Mock private JwtUtils jwtUtils;

  @Mock private UserDetailsServiceImpl userDetailsService;

  @Mock private TokenBlacklistService tokenBlacklistService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private AuthTokenFilter authTokenFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_NoToken_ShouldProceedWithoutAuth() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_InvalidToken_ShouldProceedWithoutAuth()
      throws ServletException, IOException {
    String token = "invalidToken";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(false);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_BlacklistedToken_ShouldReturnUnauthorized()
      throws ServletException, IOException {
    String token = "blacklistedToken";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

    PrintWriter writer = new PrintWriter(new StringWriter());
    when(response.getWriter()).thenReturn(writer);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_TokenIssuedBeforeForcedLogout_ShouldReturnUnauthorized()
      throws ServletException, IOException {
    String token = "oldToken";
    String userId = UUID.randomUUID().toString();
    Date issuedAt = new Date(1000L);
    long forcedLogoutAt = 2000L;

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
    when(tokenBlacklistService.getForcedLogoutTimestamp(UUID.fromString(userId)))
        .thenReturn(forcedLogoutAt);
    when(jwtUtils.getIssuedAtFromJwtToken(token)).thenReturn(issuedAt);

    PrintWriter writer = new PrintWriter(new StringWriter());
    when(response.getWriter()).thenReturn(writer);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_MissingEmail_ShouldReturnUnauthorized()
      throws ServletException, IOException {
    String token = "noEmailToken";
    String userId = UUID.randomUUID().toString();
    Date issuedAt = new Date(3000L);
    long forcedLogoutAt = 2000L;

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
    when(tokenBlacklistService.getForcedLogoutTimestamp(UUID.fromString(userId)))
        .thenReturn(forcedLogoutAt);
    when(jwtUtils.getIssuedAtFromJwtToken(token)).thenReturn(issuedAt);
    when(jwtUtils.getEmailFromJwtToken(token)).thenReturn(null);

    PrintWriter writer = new PrintWriter(new StringWriter());
    when(response.getWriter()).thenReturn(writer);

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternal_ValidToken_ShouldAuthenticate() throws ServletException, IOException {
    String token = "validToken";
    String userId = UUID.randomUUID().toString();
    String email = "test@example.com";
    Date issuedAt = new Date(3000L);
    long forcedLogoutAt = 2000L;

    UserDetails userDetails = mock(UserDetails.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);
    when(tokenBlacklistService.getForcedLogoutTimestamp(UUID.fromString(userId)))
        .thenReturn(forcedLogoutAt);
    when(jwtUtils.getIssuedAtFromJwtToken(token)).thenReturn(issuedAt);
    when(jwtUtils.getEmailFromJwtToken(token)).thenReturn(email);
    when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
    when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

    authTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(
        userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }
}
