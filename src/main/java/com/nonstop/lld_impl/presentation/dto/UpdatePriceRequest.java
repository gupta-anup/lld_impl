package com.nonstop.lld_impl.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Update Price Request DTO
 * 
 * LLD Principles Applied:
 * 1. Single Responsibility: Only handles price update data
 * 2. Command Pattern: Represents a price update command
 * 3. Validation at Boundaries: Ensures valid price input
 */
public class UpdatePriceRequest {
    
    @NotNull(message = "New price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal newPrice;
    
    // Default constructor for JSON deserialization
    public UpdatePriceRequest() {}
    
    public UpdatePriceRequest(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }
    
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
    
    @Override
    public String toString() {
        return String.format("UpdatePriceRequest{newPrice=%s}", newPrice);
    }
}
