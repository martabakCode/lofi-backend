package com.lofi.lofiapps.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lofi.lofiapps.dto.request.CreateProductRequest;
import com.lofi.lofiapps.dto.response.ProductResponse;
import com.lofi.lofiapps.entity.Product;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductDtoMapperTest {

  private ProductDtoMapper productDtoMapper;

  @BeforeEach
  void setUp() {
    productDtoMapper = new ProductDtoMapper();
  }

  // ===== toDomain Tests =====

  @Test
  @DisplayName("ToDomain should return null when request is null")
  void toDomain_ShouldReturnNull_WhenRequestIsNull() {
    // Act
    Product result = productDtoMapper.toDomain(null);

    // Assert
    assertNull(result);
  }

  @Test
  @DisplayName("ToDomain should map request to domain correctly")
  void toDomain_ShouldMapRequestToDomainCorrectly() {
    // Arrange
    CreateProductRequest request = new CreateProductRequest();
    request.setProductCode("PROD001");
    request.setProductName("Personal Loan");
    request.setDescription("A personal loan product");
    request.setInterestRate(BigDecimal.valueOf(12.5));
    request.setAdminFee(BigDecimal.valueOf(100000));
    request.setMinTenor(6);
    request.setMaxTenor(36);
    request.setMinLoanAmount(BigDecimal.valueOf(1000000));
    request.setMaxLoanAmount(BigDecimal.valueOf(100000000));

    // Act
    Product result = productDtoMapper.toDomain(request);

    // Assert
    assertNotNull(result);
    assertEquals("PROD001", result.getProductCode());
    assertEquals("Personal Loan", result.getProductName());
    assertEquals("A personal loan product", result.getDescription());
    assertEquals(BigDecimal.valueOf(12.5), result.getInterestRate());
    assertEquals(BigDecimal.valueOf(100000), result.getAdminFee());
    assertEquals(6, result.getMinTenor());
    assertEquals(36, result.getMaxTenor());
    assertEquals(BigDecimal.valueOf(1000000), result.getMinLoanAmount());
    assertEquals(BigDecimal.valueOf(100000000), result.getMaxLoanAmount());
    assertTrue(result.getIsActive()); // default true
  }

  @Test
  @DisplayName("ToDomain should set isActive to true by default")
  void toDomain_ShouldSetIsActiveToTrueByDefault() {
    // Arrange
    CreateProductRequest request = new CreateProductRequest();
    request.setProductCode("PROD002");
    request.setProductName("Business Loan");
    request.setInterestRate(BigDecimal.valueOf(10.0));
    request.setAdminFee(BigDecimal.valueOf(50000));
    request.setMinTenor(12);
    request.setMaxTenor(60);
    request.setMinLoanAmount(BigDecimal.valueOf(5000000));
    request.setMaxLoanAmount(BigDecimal.valueOf(500000000));

    // Act
    Product result = productDtoMapper.toDomain(request);

    // Assert
    assertNotNull(result);
    assertTrue(result.getIsActive());
  }

  @Test
  @DisplayName("ToDomain should handle null description")
  void toDomain_ShouldHandleNullDescription() {
    // Arrange
    CreateProductRequest request = new CreateProductRequest();
    request.setProductCode("PROD003");
    request.setProductName("Simple Loan");
    request.setDescription(null);
    request.setInterestRate(BigDecimal.valueOf(15.0));
    request.setAdminFee(BigDecimal.valueOf(25000));
    request.setMinTenor(3);
    request.setMaxTenor(12);
    request.setMinLoanAmount(BigDecimal.valueOf(500000));
    request.setMaxLoanAmount(BigDecimal.valueOf(10000000));

    // Act
    Product result = productDtoMapper.toDomain(request);

    // Assert
    assertNotNull(result);
    assertNull(result.getDescription());
  }

  // ===== toResponse Tests =====

  @Test
  @DisplayName("ToResponse should return null when product is null")
  void toResponse_ShouldReturnNull_WhenProductIsNull() {
    // Act
    ProductResponse result = productDtoMapper.toResponse(null);

    // Assert
    assertNull(result);
  }

  @Test
  @DisplayName("ToResponse should map product to response correctly")
  void toResponse_ShouldMapProductToResponseCorrectly() {
    // Arrange
    UUID productId = UUID.randomUUID();
    Product product =
        Product.builder()
            .id(productId)
            .productCode("PROD001")
            .productName("Personal Loan")
            .description("A personal loan product")
            .interestRate(BigDecimal.valueOf(12.5))
            .adminFee(BigDecimal.valueOf(100000))
            .minTenor(6)
            .maxTenor(36)
            .minLoanAmount(BigDecimal.valueOf(1000000))
            .maxLoanAmount(BigDecimal.valueOf(100000000))
            .isActive(true)
            .build();

    // Act
    ProductResponse result = productDtoMapper.toResponse(product);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId());
    assertEquals("PROD001", result.getProductCode());
    assertEquals("Personal Loan", result.getProductName());
    assertEquals("A personal loan product", result.getDescription());
    assertEquals(BigDecimal.valueOf(12.5), result.getInterestRate());
    assertEquals(BigDecimal.valueOf(100000), result.getAdminFee());
    assertEquals(6, result.getMinTenor());
    assertEquals(36, result.getMaxTenor());
    assertEquals(BigDecimal.valueOf(1000000), result.getMinLoanAmount());
    assertEquals(BigDecimal.valueOf(100000000), result.getMaxLoanAmount());
    assertTrue(result.getIsActive());
  }

  @Test
  @DisplayName("ToResponse should map inactive product correctly")
  void toResponse_ShouldMapInactiveProductCorrectly() {
    // Arrange
    UUID productId = UUID.randomUUID();
    Product product =
        Product.builder()
            .id(productId)
            .productCode("PROD002")
            .productName("Discontinued Loan")
            .interestRate(BigDecimal.valueOf(10.0))
            .adminFee(BigDecimal.valueOf(50000))
            .minTenor(12)
            .maxTenor(24)
            .minLoanAmount(BigDecimal.valueOf(2000000))
            .maxLoanAmount(BigDecimal.valueOf(50000000))
            .isActive(false)
            .build();

    // Act
    ProductResponse result = productDtoMapper.toResponse(product);

    // Assert
    assertNotNull(result);
    assertFalse(result.getIsActive());
  }

  @Test
  @DisplayName("ToResponse should handle null description")
  void toResponse_ShouldHandleNullDescription() {
    // Arrange
    UUID productId = UUID.randomUUID();
    Product product =
        Product.builder()
            .id(productId)
            .productCode("PROD003")
            .productName("No Description Loan")
            .description(null)
            .interestRate(BigDecimal.valueOf(8.0))
            .adminFee(BigDecimal.valueOf(75000))
            .minTenor(6)
            .maxTenor(18)
            .minLoanAmount(BigDecimal.valueOf(1500000))
            .maxLoanAmount(BigDecimal.valueOf(30000000))
            .isActive(true)
            .build();

    // Act
    ProductResponse result = productDtoMapper.toResponse(product);

    // Assert
    assertNotNull(result);
    assertNull(result.getDescription());
  }

  @Test
  @DisplayName("ToResponse should preserve precision for BigDecimal values")
  void toResponse_ShouldPreservePrecisionForBigDecimalValues() {
    // Arrange
    UUID productId = UUID.randomUUID();
    BigDecimal interestRate = new BigDecimal("15.75");
    BigDecimal adminFee = new BigDecimal("150000.50");
    BigDecimal minLoanAmount = new BigDecimal("1000000.00");
    BigDecimal maxLoanAmount = new BigDecimal("999999999.99");

    Product product =
        Product.builder()
            .id(productId)
            .productCode("PROD004")
            .productName("Precision Test Loan")
            .interestRate(interestRate)
            .adminFee(adminFee)
            .minTenor(1)
            .maxTenor(120)
            .minLoanAmount(minLoanAmount)
            .maxLoanAmount(maxLoanAmount)
            .isActive(true)
            .build();

    // Act
    ProductResponse result = productDtoMapper.toResponse(product);

    // Assert
    assertNotNull(result);
    assertEquals(interestRate, result.getInterestRate());
    assertEquals(adminFee, result.getAdminFee());
    assertEquals(minLoanAmount, result.getMinLoanAmount());
    assertEquals(maxLoanAmount, result.getMaxLoanAmount());
  }

  // ===== Roundtrip Tests =====

  @Test
  @DisplayName("Roundtrip mapping should preserve data")
  void roundtripMapping_ShouldPreserveData() {
    // Arrange
    CreateProductRequest request = new CreateProductRequest();
    request.setProductCode("ROUNDTRIP001");
    request.setProductName("Roundtrip Test Loan");
    request.setDescription("Testing roundtrip mapping");
    request.setInterestRate(BigDecimal.valueOf(11.25));
    request.setAdminFee(BigDecimal.valueOf(85000));
    request.setMinTenor(3);
    request.setMaxTenor(48);
    request.setMinLoanAmount(BigDecimal.valueOf(750000));
    request.setMaxLoanAmount(BigDecimal.valueOf(75000000));

    // Act
    Product domain = productDtoMapper.toDomain(request);
    ProductResponse response = productDtoMapper.toResponse(domain);

    // Assert
    assertNotNull(response);
    assertEquals(request.getProductCode(), response.getProductCode());
    assertEquals(request.getProductName(), response.getProductName());
    assertEquals(request.getDescription(), response.getDescription());
    assertEquals(request.getInterestRate(), response.getInterestRate());
    assertEquals(request.getAdminFee(), response.getAdminFee());
    assertEquals(request.getMinTenor(), response.getMinTenor());
    assertEquals(request.getMaxTenor(), response.getMaxTenor());
    assertEquals(request.getMinLoanAmount(), response.getMinLoanAmount());
    assertEquals(request.getMaxLoanAmount(), response.getMaxLoanAmount());
    assertTrue(response.getIsActive());
  }
}
