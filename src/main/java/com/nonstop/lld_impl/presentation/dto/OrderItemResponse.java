package com.nonstop.lld_impl.presentation.dto;

import java.math.BigDecimal;

/**
 * Order Item Response DTO
 * 
 * LLD Principles Applied:
 * 1. Value Object Pattern: Represents order item data
 * 2. Immutable Response Objects
 * 3. Information Expert: Contains necessary order item information
 */
public class OrderItemResponse {
    
    private final Long id;
    private final Long productId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    
    public OrderItemResponse(Long id, Long productId, String productName,
                           Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
    
    // Getters only (immutable)
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    
    @Override
    public String toString() {
        return String.format("OrderItemResponse{id=%d, productName='%s', quantity=%d, subtotal=%s}", 
                           id, productName, quantity, subtotal);
    }
}
