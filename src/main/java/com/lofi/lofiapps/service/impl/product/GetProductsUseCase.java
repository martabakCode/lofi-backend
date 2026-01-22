package com.lofi.lofiapps.service.impl.product;

import com.lofi.lofiapps.mapper.ProductDtoMapper;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductsUseCase {
  private final ProductRepository productRepository;
  private final ProductDtoMapper productDtoMapper;

  @Transactional(readOnly = true)
  public PagedResponse<ProductResponse> execute(Boolean isActive, Pageable pageable) {
    Page<Product> page = productRepository.findAll(isActive, pageable);

    List<ProductResponse> items =
        page.getContent().stream().map(productDtoMapper::toResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }
}
