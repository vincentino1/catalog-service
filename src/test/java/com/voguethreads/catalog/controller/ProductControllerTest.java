package com.voguethreads.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voguethreads.catalog.dto.PagedResponse;
import com.voguethreads.catalog.dto.ProductRequest;
import com.voguethreads.catalog.dto.ProductResponse;
import com.voguethreads.catalog.service.ProductService;
import com.voguethreads.catalog.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProductResponse sampleProduct(String id) {
        return ProductResponse.builder()
                .id(id)
                .sku("SKU-123")
                .name("Sample Tee")
                .description("A comfy t-shirt")
                .price(ProductResponse.PriceInfo.builder().currency("USD").amount(1999).build())
                .inventory(ProductResponse.InventoryInfo.builder().inStock(true).quantity(50).build())
                .category("tops")
                .tags(List.of("men", "summer"))
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-02T00:00:00Z"))
                .build();
    }

    @Test
    @DisplayName("GET /products should return paged list of products")
    void listProducts_success() throws Exception {
        PagedResponse<ProductResponse> paged = PagedResponse.<ProductResponse>builder()
                .items(List.of(sampleProduct("p1"), sampleProduct("p2")))
                .page(1)
                .pageSize(2)
                .totalItems(10L)
                .totalPages(5)
                .build();

        when(productService.listProducts(eq(1), eq(2), eq("shirt"), eq("tops"))).thenReturn(paged);

        mockMvc.perform(get("/products")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("query", "shirt")
                        .param("category", "tops"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.totalItems", is(10)))
                .andExpect(jsonPath("$.totalPages", is(5)))
                .andExpect(jsonPath("$.items[0].sku", is("SKU-123")))
                .andExpect(jsonPath("$.items[0].price.currency", is("USD")))
                .andExpect(jsonPath("$.items[0].inventory.inStock", is(true)));

        verify(productService, times(1)).listProducts(1, 2, "shirt", "tops");
    }

    @Test
    @DisplayName("GET /products/{id} should return a single product")
    void getProduct_success() throws Exception {
        when(productService.getProductByIdString("abc")).thenReturn(sampleProduct("abc"));

        mockMvc.perform(get("/products/{id}", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("abc")))
                .andExpect(jsonPath("$.sku", is("SKU-123")))
                .andExpect(jsonPath("$.price.amount", is(1999)))
                .andExpect(jsonPath("$.inventory.quantity", is(50)));

        verify(productService, times(1)).getProductByIdString("abc");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /products should create and return Product with 201 when ADMIN")
    void createProduct_asAdmin_success() throws Exception {
        ProductRequest req = ProductRequest.builder()
                .sku("SKU-NEW")
                .name("New Tee")
                .description("Nice one")
                .currency("USD")
                .amount(2999)
                .quantity(10)
                .category("tops")
                .tags(List.of("new"))
                .build();

        ProductResponse created = sampleProduct("new-id");
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(created);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("new-id")))
                .andExpect(jsonPath("$.name", is("Sample Tee")));

        ArgumentCaptor<ProductRequest> captor = ArgumentCaptor.forClass(ProductRequest.class);
        verify(productService, times(1)).createProduct(captor.capture());
        ProductRequest captured = captor.getValue();
        // Basic verification that mapping went through
        assert captured.getSku().equals("SKU-NEW");
        assert captured.getAmount() == 2999;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /products/{id} should update and return Product when ADMIN")
    void updateProduct_asAdmin_success() throws Exception {
        ProductRequest req = ProductRequest.builder()
                .sku("SKU-UPD")
                .name("Updated Tee")
                .description("Updated desc")
                .currency("USD")
                .amount(2499)
                .quantity(20)
                .category("tops")
                .tags(List.of("updated"))
                .build();

        ProductResponse updated = sampleProduct("abc");
        updated.setName("Updated Tee");
        when(productService.updateProductByIdString(eq("abc"), any(ProductRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/products/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("abc")))
                .andExpect(jsonPath("$.name", is("Updated Tee")));

        verify(productService, times(1)).updateProductByIdString(eq("abc"), any(ProductRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /products/{id} should return 204 when ADMIN")
    void deleteProduct_asAdmin_success() throws Exception {
        doNothing().when(productService).deleteProductByIdString("abc");

        mockMvc.perform(delete("/products/{id}", "abc"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProductByIdString("abc");
    }
}

