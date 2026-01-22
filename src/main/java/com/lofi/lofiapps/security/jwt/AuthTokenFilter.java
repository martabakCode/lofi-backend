package com.lofi.lofiapps.security.jwt;

import com.lofi.lofiapps.security.service.TokenBlacklistService;
import com.lofi.lofiapps.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final UserDetailsServiceImpl userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        if (tokenBlacklistService.isBlacklisted(jwt)) {
          log.warn("Access denied: Token is blacklisted");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().write("Token is invalidated (logged out).");
          return;
        }

        // Check for forced logout
        UUID userId = UUID.fromString(jwtUtils.getUserIdFromJwtToken(jwt));
        long forcedLogoutAt = tokenBlacklistService.getForcedLogoutTimestamp(userId);
        long tokenIssuedAt = jwtUtils.getIssuedAtFromJwtToken(jwt).getTime();

        if (tokenIssuedAt < forcedLogoutAt) {
          log.warn("Access denied: Token invalidated by admin force logout");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().write("Session invalidated by admin. Please login again.");
          return;
        }

        // Using email claim to load user, as UserDetailsService expects a string
        // identifier (usually username/email)
        String email = jwtUtils.getEmailFromJwtToken(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      log.error("Cannot set user authentication: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
