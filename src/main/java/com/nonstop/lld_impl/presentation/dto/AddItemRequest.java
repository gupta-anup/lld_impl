package com.nonstop.lld_impl.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Add Item to Order Request DTO
 * 
 * LLD Principles Applied:
 * 1. Command Pattern: Represents an add item command
 * 2. Validation at Input Boundaries
 * 3. Immutable Request Objects
 */
public class AddItemRequest {
    
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    // Default constructor for JSON deserialization
    public AddItemRequest() {}
    
    public AddItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    @Override
    public String toString() {
        return String.format("AddItemRequest{productId=%d, quantity=%d}", productId, quantity);
    }
}
