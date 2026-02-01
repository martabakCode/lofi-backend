package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.CreateProductRequest;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.dto.response.ProductResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductControllerTest {

  private MockMvc mockMvc;

  @Mock private ProductService productService;

  @InjectMocks private ProductController productController;

  private ObjectMapper objectMapper;

  private UUID userId;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(productController)
            .setCustomArgumentResolvers(
                new AuthenticationPrincipalArgumentResolver(),
                new PageableHandlerMethodArgumentResolver())
            .build();
    objectMapper = new ObjectMapper();
    userId = UUID.randomUUID();
    setupSecurityContext(userId);
  }

  private void setupSecurityContext(UUID userId) {
    com.lofi.lofiapps.entity.User user =
        com.lofi.lofiapps.entity.User.builder()
            .id(userId)
            .email("test@example.com")
            .password("password")
            .status(com.lofi.lofiapps.enums.UserStatus.ACTIVE)
            .roles(java.util.Collections.emptySet())
            .build();

    UserPrincipal userPrincipal = UserPrincipal.create(user);

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Create product should return created product")
  void createProduct_ShouldReturnCreatedProduct() throws Exception {
    CreateProductRequest request = new CreateProductRequest();
    request.setProductName("Test Product");
    request.setProductCode("PROD001");
    // Fill all required fields
    request.setInterestRate(BigDecimal.valueOf(10));
    request.setAdminFee(BigDecimal.valueOf(5000));
    request.setMinTenor(3);
    request.setMaxTenor(12);
    request.setMinLoanAmount(BigDecimal.valueOf(100000));
    request.setMaxLoanAmount(BigDecimal.valueOf(10000000));

    ProductResponse response =
        ProductResponse.builder().id(UUID.randomUUID()).productName("Test Product").build();

    when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.productName").value("Test Product"));

    verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
  }

  @Test
  @DisplayName("Get products should return paged response")
  void getProducts_ShouldReturnPagedResponse() throws Exception {
    PagedResponse<ProductResponse> pagedResponse = new PagedResponse<>();
    pagedResponse.setItems(List.of(ProductResponse.builder().build()));
    pagedResponse.setMeta(new PagedResponse.Meta(1, 10, 1, 1));

    // Matcher for Pageable
    when(productService.getProducts(any(), any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get("/products").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(productService, times(1)).getProducts(any(), any(Pageable.class));
  }

  @Test
  @DisplayName("Recommend product should return recommendation")
  void recommendProduct_ShouldReturnRecommendation() throws Exception {
    ProductRecommendationResponse response = new ProductRecommendationResponse();
    // Assuming fields for response

    when(productService.recommendProduct(eq(userId))).thenReturn(response);

    mockMvc
        .perform(get("/products/recommendation"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(productService, times(1)).recommendProduct(eq(userId));
  }

  @Test
  @DisplayName("Get assigned product should return product response")
  void getAssignedProduct_ShouldReturnProductResponse() throws Exception {
    ProductResponse response = ProductResponse.builder().id(UUID.randomUUID()).build();

    when(productService.getAssignedProduct(eq(userId))).thenReturn(response);

    mockMvc
        .perform(get("/products/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(productService, times(1)).getAssignedProduct(eq(userId));
  }
}
