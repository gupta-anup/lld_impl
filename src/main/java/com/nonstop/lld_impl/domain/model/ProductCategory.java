package com.nonstop.lld_impl.domain.model;

/**
 * Product Category Enum
 * 
 * LLD Principle: Enumerated Types for Type Safety
 * - Prevents invalid category values
 * - Provides compile-time safety
 * - Easy to extend without breaking existing code
 */
public enum ProductCategory {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing"),
    BOOKS("Books"),
    HOME_GARDEN("Home & Garden"),
    SPORTS("Sports"),
    TOYS("Toys"),
    BEAUTY("Beauty"),
    AUTOMOTIVE("Automotive"),
    FOOD("Food"),
    OTHER("Other");
    
    private final String displayName;
    
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
