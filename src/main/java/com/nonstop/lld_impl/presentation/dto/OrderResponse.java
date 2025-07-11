package com.nonstop.lld_impl.presentation.dto;

import com.nonstop.lld_impl.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTO
 * 
 * LLD Principles Applied:
 * 1. Aggregate Response Pattern: Contains order with its items
 * 2. Immutable Response Objects
 * 3. Composite Pattern: Order contains order items
 * 4. Information Expert: Complete order information for clients
 */
public class OrderResponse {
    
    private final Long id;
    private final Long customerId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<OrderItemResponse> items;
    
    public OrderResponse(Long id, Long customerId, OrderStatus status,
                        BigDecimal totalAmount, LocalDateTime createdAt,
                        LocalDateTime updatedAt, List<OrderItemResponse> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }
    
    // Getters only (immutable)
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItemResponse> getItems() { return items; }
    
    @Override
    public String toString() {
        return String.format("OrderResponse{id=%d, customerId=%d, status=%s, total=%s, items=%d}", 
                           id, customerId, status, totalAmount, items.size());
    }
}
