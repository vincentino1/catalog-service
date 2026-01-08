package com.voguethreads.catalog.service;

import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.exception.DuplicateSkuException;
import com.voguethreads.catalog.exception.ProductNotFoundException;
import com.voguethreads.catalog.mapper.ProductMapper;
import com.voguethreads.catalog.model.Product;
import com.voguethreads.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_WithValidRequest_ShouldCreateProduct() {
        ProductRequest request = createTestProductRequest();
        Product product = createTestProduct();
        ProductResponse response = createTestProductResponse();

        when(productRepository.existsBySku(request.getSku())).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("prod_1", result.getId());
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_WithDuplicateSku_ShouldThrowException() {
        ProductRequest request = createTestProductRequest();

        when(productRepository.existsBySku(request.getSku())).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        Product product = createTestProduct();
        ProductResponse response = createTestProductResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("prod_1", result.getId());
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void deleteProduct_WithValidId_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void decrementInventory_WithSufficientStock_ShouldReturnTrue() {
        Product product = createTestProduct();
        product.setQuantity(100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        boolean result = productService.decrementInventory(1L, 10);

        assertTrue(result);
        verify(productRepository).save(any());
    }

    @Test
    void decrementInventory_WithInsufficientStock_ShouldReturnFalse() {
        Product product = createTestProduct();
        product.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = productService.decrementInventory(1L, 10);

        assertFalse(result);
        verify(productRepository, never()).save(any());
    }

    private ProductRequest createTestProductRequest() {
        return ProductRequest.builder()
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .currency("USD")
                .amount(2999)
                .quantity(100)
                .category("test")
                .build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .currency("USD")
                .amount(2999)
                .quantity(100)
                .inStock(true)
                .category("test")
                .build();
    }

    private ProductResponse createTestProductResponse() {
        return ProductResponse.builder()
                .id("prod_1")
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(ProductResponse.PriceInfo.builder()
                        .currency("USD")
                        .amount(2999)
                        .build())
                .inventory(ProductResponse.InventoryInfo.builder()
                        .inStock(true)
                        .quantity(100)
                        .build())
                .category("test")
                .build();
    }
}

