package com.nonstop.lld_impl.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * OrderItem Domain Entity
 * 
 * LLD Principles Applied:
 * 1. Value Object Pattern: Represents a line item in an order
 * 2. Immutability: Once created, core values don't change arbitrarily
 * 3. Encapsulation: Business logic for subtotal calculation
 * 4. Composition: Part of Order aggregate
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;
    
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    // Default constructor for JPA
    protected OrderItem() {}
    
    // Constructor
    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = Objects.requireNonNull(order, "Order cannot be null");
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        
        // Capture the price at the time of order creation
        this.unitPrice = product.getPrice();
    }
    
    // Business Logic Methods
    
    /**
     * Calculates subtotal for this line item
     * @return quantity * unitPrice
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Updates quantity of this order item
     * @param newQuantity new quantity
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        // Validate against current product stock
        if (!product.isAvailable(newQuantity)) {
            throw new IllegalStateException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                            product.getStockQuantity(), newQuantity));
        }
        
        this.quantity = newQuantity;
    }
    
    /**
     * Gets the product name for display purposes
     */
    public String getProductName() {
        return product.getName();
    }
    
    /**
     * Gets the product category for display purposes
     */
    public ProductCategory getProductCategory() {
        return product.getCategory();
    }
    
    // Getters
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("OrderItem{id=%d, product='%s', quantity=%d, unitPrice=%s, subtotal=%s}", 
                           id, getProductName(), quantity, unitPrice, getSubtotal());
    }
}
