package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.CreateProductRequest;
import com.lofi.lofiapps.dto.request.UpdateProductRequest;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.dto.response.ProductResponse;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.mapper.ProductDtoMapper;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.ProductService;
import com.lofi.lofiapps.service.impl.usecase.product.RecommendProductUseCase;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ProductDtoMapper productDtoMapper;
  private final RecommendProductUseCase recommendProductUseCase;

  private final com.lofi.lofiapps.service.ProductCalculationService productCalculationService;

  @Override
  @Transactional
  public ProductResponse createProduct(CreateProductRequest request) {
    Product product = productDtoMapper.toDomain(request);
    Product savedProduct = productRepository.save(product);
    return productDtoMapper.toResponse(savedProduct);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<ProductResponse> getProducts(Boolean isActive, Pageable pageable) {
    Page<Product> page;
    if (isActive != null) {
      page = productRepository.findByIsActive(isActive, pageable);
    } else {
      page = productRepository.findAll(pageable);
    }

    List<ProductResponse> items =
        page.getContent().stream().map(productDtoMapper::toResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  @Override
  public ProductRecommendationResponse recommendProduct(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    List<Product> products = productRepository.findByIsActiveTrue();

    return recommendProductUseCase.execute(user, products);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getAssignedProduct(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    Product product = user.getProduct();
    if (product == null) {
      throw new IllegalArgumentException("No product assigned to this user");
    }

    return productDtoMapper.toResponse(product);
  }

  @Override
  @Transactional(readOnly = true)
  public com.lofi.lofiapps.dto.response.AvailableProductResponse getAvailableProduct(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    Product product = user.getProduct();
    if (product == null) {
      throw new IllegalArgumentException("No product assigned to user");
    }

    return mapToAvailableProductResponse(user, product);
  }

  @Override
  @Transactional(readOnly = true)
  public List<com.lofi.lofiapps.dto.response.AvailableProductResponse> getAllAvailableProducts(
      UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    List<Product> activeProducts = productRepository.findByIsActiveTrue();

    return activeProducts.stream()
        .map(product -> mapToAvailableProductResponse(user, product))
        .collect(Collectors.toList());
  }

  private com.lofi.lofiapps.dto.response.AvailableProductResponse mapToAvailableProductResponse(
      User user, Product product) {

    java.math.BigDecimal availableAmount =
        productCalculationService.calculateAvailableAmount(user.getId(), product.getId());
    boolean hasSubmittedLoan = productCalculationService.hasActiveLoan(user.getId());

    java.math.BigDecimal approvedLoanAmount = java.math.BigDecimal.ZERO;
    com.lofi.lofiapps.enums.LoanStatus lastLoanStatus = null;
    java.time.LocalDateTime lastLoanSubmittedAt = null;

    java.util.List<com.lofi.lofiapps.entity.Loan> loans =
        productCalculationService.getActiveLoans(user.getId());
    if (!loans.isEmpty()) {
      approvedLoanAmount =
          loans.stream()
              .map(com.lofi.lofiapps.entity.Loan::getLoanAmount)
              .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
      // Assuming list is ordered by recency or just taking first as "last" relevant
      // one for status
      // display
      // Ideally query should order by date desc
      lastLoanStatus = loans.get(0).getLoanStatus();
      lastLoanSubmittedAt = loans.get(0).getSubmittedAt();
    }

    return com.lofi.lofiapps.dto.response.AvailableProductResponse.builder()
        .productId(product.getId())
        .productCode(product.getProductCode())
        .productName(product.getProductName())
        .productLimit(product.getMaxLoanAmount())
        .approvedLoanAmount(approvedLoanAmount)
        .availableAmount(availableAmount)
        .hasSubmittedLoan(hasSubmittedLoan)
        .lastLoanStatus(lastLoanStatus)
        .lastLoanSubmittedAt(lastLoanSubmittedAt)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getProductById(UUID id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
    return productDtoMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));

    product.setProductName(request.getProductName());
    product.setDescription(request.getDescription());
    product.setInterestRate(request.getInterestRate());
    product.setMinTenor(request.getMinTenor());
    product.setMaxTenor(request.getMaxTenor());
    product.setMinLoanAmount(request.getMinLoanAmount());
    product.setMaxLoanAmount(request.getMaxLoanAmount());
    product.setAdminFee(request.getAdminFee());
    product.setIsActive(request.getIsActive());

    Product updatedProduct = productRepository.save(product);
    return productDtoMapper.toResponse(updatedProduct);
  }

  @Override
  @Transactional
  public void deleteProduct(UUID id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
    productRepository.delete(product);
  }
}
