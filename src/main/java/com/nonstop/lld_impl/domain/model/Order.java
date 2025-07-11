package com.nonstop.lld_impl.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order Domain Entity
 * 
 * LLD Principles Applied:
 * 1. Aggregate Root: Order manages OrderItems as an aggregate
 * 2. Domain-Driven Design: Rich business logic encapsulated
 * 3. State Pattern: Order status transitions with validation
 * 4. Composition over Inheritance: Order contains OrderItems
 */
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(nullable = false)
    private Long customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Default constructor for JPA
    protected Order() {}
    
    // Constructor
    public Order(Long customerId) {
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    
    /**
     * Adds an item to the order
     * Can only add items when order is in PENDING status
     */
    public void addItem(Product product, int quantity) {
        validateOrderCanBeModified();
        validateProductAndQuantity(product, quantity);
        
        // Check if product already exists in order, update quantity
        OrderItem existingItem = findOrderItemByProduct(product);
        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + quantity);
        } else {
            OrderItem newItem = new OrderItem(this, product, quantity);
            orderItems.add(newItem);
        }
        
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Removes an item from the order
     */
    public void removeItem(Product product) {
        validateOrderCanBeModified();
        
        OrderItem itemToRemove = findOrderItemByProduct(product);
        if (itemToRemove != null) {
            orderItems.remove(itemToRemove);
            recalculateTotal();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Confirms the order - changes status to CONFIRMED
     * Can only confirm PENDING orders
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm PENDING orders");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm empty order");
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Processes the order - changes status to PROCESSING
     * Can only process CONFIRMED orders
     */
    public void process() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only process CONFIRMED orders");
        }
        
        this.status = OrderStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Ships the order - changes status to SHIPPED
     * Can only ship PROCESSING orders
     */
    public void ship() {
        if (status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Can only ship PROCESSING orders");
        }
        
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Delivers the order - changes status to DELIVERED
     * Can only deliver SHIPPED orders
     */
    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Can only deliver SHIPPED orders");
        }
        
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Cancels the order - changes status to CANCELLED
     * Can only cancel PENDING or CONFIRMED orders
     */
    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only cancel PENDING or CONFIRMED orders");
        }
        
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if order can be modified (add/remove items)
     */
    public boolean canBeModified() {
        return status == OrderStatus.PENDING;
    }
    
    // Private helper methods
    
    private void validateOrderCanBeModified() {
        if (!canBeModified()) {
            throw new IllegalStateException("Order cannot be modified in " + status + " status");
        }
    }
    
    private void validateProductAndQuantity(Product product, int quantity) {
        Objects.requireNonNull(product, "Product cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!product.isAvailable(quantity)) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
    }
    
    private OrderItem findOrderItemByProduct(Product product) {
        return orderItems.stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }
    
    private void recalculateTotal() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getOrderItems() { return new ArrayList<>(orderItems); } // Defensive copy
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Order{id=%d, customerId=%d, status=%s, total=%s, items=%d}", 
                           id, customerId, status, totalAmount, orderItems.size());
    }
}
