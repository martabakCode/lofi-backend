package com.lofi.lofiapps.model.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
  private List<T> items;
  private Meta meta;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
  }

  public static <T> PagedResponse<T> of(
      List<T> items, int page, int size, long totalItems, int totalPages) {
    return PagedResponse.<T>builder()
        .items(items)
        .meta(
            Meta.builder()
                .page(page)
                .size(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build())
        .build();
  }
}
