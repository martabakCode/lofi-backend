package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.ProductMapper;
import com.lofi.lofiapps.model.entity.JpaProduct;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.repository.JpaProductRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepository {
  private final JpaProductRepository jpaProductRepository;
  private final ProductMapper productMapper;

  @Override
  public Product save(Product product) {
    JpaProduct jpaProduct = productMapper.toJpa(product);
    JpaProduct saved = jpaProductRepository.save(jpaProduct);
    return productMapper.toDomain(saved);
  }

  @Override
  public Optional<Product> findById(UUID id) {
    return jpaProductRepository.findById(id).map(productMapper::toDomain);
  }

  @Override
  public Optional<Product> findByProductCode(String productCode) {
    return jpaProductRepository.findByProductCode(productCode).map(productMapper::toDomain);
  }

  @Override
  public List<Product> findAllActive() {
    return jpaProductRepository.findByIsActiveTrue().stream()
        .map(productMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Product> findAll(Boolean isActive, Pageable pageable) {
    if (isActive != null) {
      return jpaProductRepository.findByIsActive(isActive, pageable).map(productMapper::toDomain);
    }
    return jpaProductRepository.findAll(pageable).map(productMapper::toDomain);
  }
}
