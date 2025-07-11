package com.nonstop.lld_impl.domain.model;

/**
 * Order Status Enum
 * 
 * LLD Principle: State Pattern Implementation
 * - Represents valid order states
 * - Enables state transition validation
 * - Type-safe state management
 */
public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if transition to target status is valid
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == PROCESSING || targetStatus == CANCELLED;
            case PROCESSING -> targetStatus == SHIPPED;
            case SHIPPED -> targetStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
