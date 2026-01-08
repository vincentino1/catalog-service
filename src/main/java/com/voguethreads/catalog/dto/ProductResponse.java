package com.voguethreads.catalog.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String sku;
    private String name;
    private String description;
    private PriceInfo price;
    private InventoryInfo inventory;
    private String category;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceInfo {
        private String currency;
        private Integer amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryInfo {
        private Boolean inStock;
        private Integer quantity;
    }
}

