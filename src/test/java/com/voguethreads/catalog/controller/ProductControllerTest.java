package com.voguethreads.catalog.controller;

import com.voguethreads.catalog.dto.PagedResponse;
import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void listProducts_ShouldReturnPagedResponse() throws Exception {
        ProductResponse product = createTestProductResponse();
        PagedResponse<ProductResponse> pagedResponse = PagedResponse.<ProductResponse>builder()
                .items(Arrays.asList(product))
                .page(1)
                .pageSize(20)
                .totalItems(1L)
                .totalPages(1)
                .build();

        when(productService.listProducts(any(), any(), any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void getProduct_ShouldReturnProduct() throws Exception {
        ProductResponse product = createTestProductResponse();
        when(productService.getProductByIdString("prod_1")).thenReturn(product);

        mockMvc.perform(get("/products/prod_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod_1"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithAdminRole_ShouldCreateProduct() throws Exception {
        ProductResponse product = createTestProductResponse();
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(product);

        String requestJson = """
                {
                    "sku": "TEST-001",
                    "name": "Test Product",
                    "description": "Test Description",
                    "currency": "USD",
                    "amount": 2999,
                    "quantity": 100,
                    "category": "test",
                    "tags": ["test"]
                }
                """;

        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("prod_1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_WithUserRole_ShouldReturnForbidden() throws Exception {
        String requestJson = """
                {
                    "sku": "TEST-001",
                    "name": "Test Product",
                    "description": "Test Description",
                    "currency": "USD",
                    "amount": 2999,
                    "quantity": 100
                }
                """;

        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
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
                .tags(Arrays.asList("test"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

