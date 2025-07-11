package com.nonstop.lld_impl.presentation.dto;

import java.math.BigDecimal;

import com.nonstop.lld_impl.domain.model.ProductCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Create Product Request DTO
 * 
 * LLD Principles Applied:
 * 1. Data Transfer Object Pattern: Encapsulates request data
 * 2. Validation at Boundaries: Input validation using annotations
 * 3. Immutable DTOs: Prevents accidental modification
 * 4. Single Responsibility: Only handles product creation data
 */
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be non-negative")
    private Integer stockQuantity;
    
    @NotNull(message = "Category is required")
    private ProductCategory category;
    
    // Default constructor for JSON deserialization
    public CreateProductRequest() {}
    
    // Constructor
    public CreateProductRequest(String name, String description, BigDecimal price, 
                              Integer stockQuantity, ProductCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public ProductCategory getCategory() { return category; }
    
    // Setters for JSON deserialization
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setCategory(ProductCategory category) { this.category = category; }
    
    @Override
    public String toString() {
        return String.format("CreateProductRequest{name='%s', price=%s, stock=%d, category=%s}", 
                           name, price, stockQuantity, category);
    }
}
