package com.voguethreads.catalog.controller;

import com.voguethreads.catalog.dto.PagedResponse;
import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> listProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category
    ) {
        log.debug("GET /products - page: {}, pageSize: {}, query: {}, category: {}",
                page, pageSize, query, category);
        PagedResponse<ProductResponse> response = productService.listProducts(page, pageSize, query, category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        log.debug("GET /products/{}", id);
        ProductResponse response = productService.getProductByIdString(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.debug("POST /products - request: {}", request);
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request
    ) {
        log.debug("PUT /products/{} - request: {}", id, request);
        ProductResponse response = productService.updateProductByIdString(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.debug("DELETE /products/{}", id);
        productService.deleteProductByIdString(id);
        return ResponseEntity.noContent().build();
    }
}

