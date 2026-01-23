package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.ProductDtoMapper;
import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductRecommendationResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.ProductService;
import com.lofi.lofiapps.service.impl.product.RecommendProductUseCase;
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
    Page<Product> page = productRepository.findAll(isActive, pageable);

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

    List<Product> products = productRepository.findAllActive();

    return recommendProductUseCase.execute(user, products);
  }
}
