package com.lofi.lofiapps.service.impl.product;

import com.lofi.lofiapps.mapper.ProductDtoMapper;
import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
  private final ProductRepository productRepository;
  private final ProductDtoMapper productDtoMapper;

  @Transactional
  public ProductResponse execute(CreateProductRequest request) {
    Product product = productDtoMapper.toDomain(request);
    Product savedProduct = productRepository.save(product);
    return productDtoMapper.toResponse(savedProduct);
  }
}
