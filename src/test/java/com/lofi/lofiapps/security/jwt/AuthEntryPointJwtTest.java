package com.lofi.lofiapps.security.jwt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private AuthenticationException authException;

  @InjectMocks private AuthEntryPointJwt authEntryPointJwt;

  private StringWriter responseWriter;

  @BeforeEach
  void setUp() throws IOException {
    responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  @Test
  @DisplayName("Commence should return 401 Unauthorized with error message")
  void commence_ShouldReturnUnauthorized() throws IOException, ServletException {
    // Arrange
    when(authException.getMessage()).thenReturn("Invalid credentials");

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
  }

  @Test
  @DisplayName("Commence should handle null exception message")
  void commence_ShouldHandleNullExceptionMessage() throws IOException, ServletException {
    // Arrange
    when(authException.getMessage()).thenReturn(null);

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
  }
}
