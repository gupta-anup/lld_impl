package com.nonstop.lld_impl.presentation.dto;

import com.nonstop.lld_impl.domain.model.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Response DTO
 * 
 * LLD Principles Applied:
 * 1. Data Transfer Object Pattern: Encapsulates response data
 * 2. Immutable Response Objects: Prevents accidental modification
 * 3. Adapter Pattern: Converts domain models to API format
 * 4. Information Expert: Contains all necessary product information for clients
 */
public class ProductResponse {
    
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final ProductCategory category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public ProductResponse(Long id, String name, String description, BigDecimal price,
                          Integer stockQuantity, ProductCategory category,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters only (immutable)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public ProductCategory getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public String toString() {
        return String.format("ProductResponse{id=%d, name='%s', price=%s, stock=%d}", 
                           id, name, price, stockQuantity);
    }
}
