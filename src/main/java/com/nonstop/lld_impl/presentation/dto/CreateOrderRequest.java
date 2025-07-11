package com.nonstop.lld_impl.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Create Order Request DTO
 * 
 * LLD Principles Applied:
 * 1. Command Pattern: Represents an order creation command
 * 2. Validation at Boundaries: Input validation
 * 3. Single Responsibility: Only handles order creation data
 */
public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    // Default constructor for JSON deserialization
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(Long customerId) {
        this.customerId = customerId;
    }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    @Override
    public String toString() {
        return String.format("CreateOrderRequest{customerId=%d}", customerId);
    }
}
