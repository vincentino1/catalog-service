package com.voguethreads.catalog.service;

import com.voguethreads.catalog.dto.PagedResponse;
import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.exception.ProductNotFoundException;
import com.voguethreads.catalog.exception.DuplicateSkuException;
import com.voguethreads.catalog.mapper.ProductMapper;
import com.voguethreads.catalog.model.Product;
import com.voguethreads.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Value("${pagination.default-page-size:20}")
    private int defaultPageSize;

    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    public PagedResponse<ProductResponse> listProducts(
            Integer page,
            Integer pageSize,
            String query,
            String category
    ) {
        log.debug("Listing products - page: {}, pageSize: {}, query: {}, category: {}",
                page, pageSize, query, category);

        int actualPage = (page != null && page > 0) ? page - 1 : 0;
        int actualPageSize = (pageSize != null && pageSize > 0)
                ? Math.min(pageSize, maxPageSize)
                : defaultPageSize;

        Pageable pageable = PageRequest.of(actualPage, actualPageSize, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.searchProducts(query, category, pageable);

        List<ProductResponse> items = productPage.getContent().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponse>builder()
                .items(items)
                .page(actualPage + 1)
                .pageSize(actualPageSize)
                .totalItems(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    public ProductResponse getProductByIdString(String idString) {
        Long id = parseProductId(idString);
        return getProductById(id);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.debug("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        log.info("Created product with id: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.debug("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        // Check if SKU is being changed to an existing one
        if (!product.getSku().equals(request.getSku()) &&
                productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException("Product with SKU '" + request.getSku() + "' already exists");
        }

        productMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);
        log.info("Updated product with id: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse updateProductByIdString(String idString, ProductRequest request) {
        Long id = parseProductId(idString);
        return updateProduct(id, request);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Deleted product with id: {}", id);
    }

    @Transactional
    public void deleteProductByIdString(String idString) {
        Long id = parseProductId(idString);
        deleteProduct(id);
    }

    @Transactional
    public boolean decrementInventory(Long productId, Integer quantity) {
        log.debug("Decrementing inventory for product {} by {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (product.getQuantity() < quantity) {
            log.warn("Insufficient stock for product {}. Available: {}, Requested: {}",
                    productId, product.getQuantity(), quantity);
            return false;
        }

        product.setQuantity(product.getQuantity() - quantity);
        product.setInStock(product.getQuantity() > 0);
        productRepository.save(product);
        log.info("Decremented inventory for product {}. New quantity: {}", productId, product.getQuantity());
        return true;
    }

    private Long parseProductId(String idString) {
        try {
            // Handle "prod_123" format
            if (idString.startsWith("prod_")) {
                return Long.parseLong(idString.substring(5));
            }
            // Handle numeric ID
            return Long.parseLong(idString);
        } catch (NumberFormatException e) {
            throw new ProductNotFoundException("Invalid product ID format: " + idString);
        }
    }
}

