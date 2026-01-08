package com.voguethreads.catalog.mapper;

import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .quantity(request.getQuantity())
                .inStock(request.getQuantity() > 0)
                .category(request.getCategory())
                .tags(request.getTags())
                .build();
    }

    public void updateEntity(Product product, ProductRequest request) {
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCurrency(request.getCurrency());
        product.setAmount(request.getAmount());
        product.setQuantity(request.getQuantity());
        product.setInStock(request.getQuantity() > 0);
        product.setCategory(request.getCategory());
        product.setTags(request.getTags());
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id("prod_" + product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(ProductResponse.PriceInfo.builder()
                        .currency(product.getCurrency())
                        .amount(product.getAmount())
                        .build())
                .inventory(ProductResponse.InventoryInfo.builder()
                        .inStock(product.getInStock())
                        .quantity(product.getQuantity())
                        .build())
                .category(product.getCategory())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

