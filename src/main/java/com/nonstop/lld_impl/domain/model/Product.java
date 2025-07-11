package com.nonstop.lld_impl.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product Domain Entity
 * 
 * LLD Principles Applied:
 * 1. Single Responsibility: Only handles product-related data and behavior
 * 2. Encapsulation: Private fields with controlled access
 * 3. Immutability: Uses defensive programming where possible
 * 4. Domain-Driven Design: Rich domain model with business logic
 */
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor for JPA
    protected Product() {}
    
    // Constructor with required fields
    public Product(String name, String description, BigDecimal price, 
                   Integer stockQuantity, ProductCategory category) {
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        this.description = description;
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.stockQuantity = Objects.requireNonNull(stockQuantity, "Stock quantity cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods (Domain behavior)
    
    /**
     * Checks if product is available for purchase
     * @param quantity requested quantity
     * @return true if available, false otherwise
     */
    public boolean isAvailable(int quantity) {
        return this.stockQuantity >= quantity && quantity > 0;
    }
    
    /**
     * Reduces stock quantity (should be called only after validation)
     * @param quantity to reduce
     * @throws IllegalStateException if insufficient stock
     */
    public void reduceStock(int quantity) {
        if (!isAvailable(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                            this.stockQuantity, quantity));
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increases stock quantity
     * @param quantity to add
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates product price
     * @param newPrice new price
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters (no setters for better encapsulation)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public ProductCategory getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%s, stock=%d}", 
                           id, name, price, stockQuantity);
    }
}
