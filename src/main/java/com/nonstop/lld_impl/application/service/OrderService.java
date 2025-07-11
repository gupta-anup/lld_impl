package com.nonstop.lld_impl.application.service;

import com.nonstop.lld_impl.domain.model.Order;
import com.nonstop.lld_impl.domain.model.OrderStatus;
import com.nonstop.lld_impl.domain.model.Product;
import com.nonstop.lld_impl.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Service Implementation
 * 
 * LLD Principles Applied:
 * 1. Facade Pattern: Simplifies complex order operations
 * 2. Strategy Pattern: Different order processing strategies
 * 3. Template Method Pattern: Common order workflow with customizable steps
 * 4. Aggregate Pattern: Order manages its OrderItems
 * 5. Domain Service: Encapsulates complex business logic
 * 6. Transaction Management: Ensures data consistency
 */
@Service
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductService productService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }
    
    /**
     * Creates a new order
     * Implements the Template Method pattern for order creation workflow
     */
    @Transactional
    public Order createOrder(Long customerId) {
        validateCustomerId(customerId);
        
        Order order = new Order(customerId);
        return orderRepository.save(order);
    }
    
    /**
     * Adds item to order
     * Implements inventory validation and stock reservation
     */
    @Transactional
    public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
        // Validate inputs
        Order order = findOrderById(orderId);
        Product product = productService.findProductById(productId);
        
        validateQuantity(quantity);
        validateOrderCanBeModified(order);
        
        // Business logic: Check stock availability
        if (!productService.isProductAvailable(productId, quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                            product.getName(), quantity, product.getStockQuantity()));
        }
        
        // Add item to order (domain logic)
        order.addItem(product, quantity);
        
        // Reserve stock
        productService.reserveStock(productId, quantity);
        
        return orderRepository.save(order);
    }
    
    /**
     * Removes item from order
     * Releases reserved stock
     */
    @Transactional
    public Order removeItemFromOrder(Long orderId, Long productId) {
        Order order = findOrderById(orderId);
        Product product = productService.findProductById(productId);
        
        validateOrderCanBeModified(order);
        
        // Find the order item to get quantity for stock release
        Integer quantityToRelease = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .map(item -> item.getQuantity())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in order"));
        
        // Remove item from order
        order.removeItem(product);
        
        // Release reserved stock
        productService.releaseStock(productId, quantityToRelease);
        
        return orderRepository.save(order);
    }
    
    /**
     * Confirms an order
     * Implements state transition validation
     */
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // Business validation
        if (order.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Cannot confirm empty order");
        }
        
        // Validate all items are still available
        validateOrderItemsAvailability(order);
        
        // Domain logic: Confirm order
        order.confirm();
        
        return orderRepository.save(order);
    }
    
    /**
     * Processes an order
     * Can be extended with different processing strategies
     */
    @Transactional
    public Order processOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // State validation
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only process confirmed orders");
        }
        
        // Business logic: Process order
        order.process();
        
        return orderRepository.save(order);
    }
    
    /**
     * Ships an order
     */
    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = findOrderById(orderId);
        order.ship();
        return orderRepository.save(order);
    }
    
    /**
     * Delivers an order
     */
    @Transactional
    public Order deliverOrder(Long orderId) {
        Order order = findOrderById(orderId);
        order.deliver();
        return orderRepository.save(order);
    }
    
    /**
     * Cancels an order
     * Releases all reserved stock
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // Release all reserved stock
        order.getOrderItems().forEach(item -> 
            productService.releaseStock(item.getProduct().getId(), item.getQuantity()));
        
        // Domain logic: Cancel order
        order.cancel();
        
        return orderRepository.save(order);
    }
    
    /**
     * Finds order by ID
     */
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }
    
    /**
     * Finds orders by customer
     */
    public List<Order> findOrdersByCustomer(Long customerId) {
        validateCustomerId(customerId);
        return orderRepository.findByCustomerId(customerId);
    }
    
    /**
     * Finds orders by status
     */
    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    /**
     * Finds recent orders for a customer
     */
    public List<Order> findRecentOrdersByCustomer(Long customerId) {
        validateCustomerId(customerId);
        return orderRepository.findRecentOrdersByCustomer(customerId);
    }
    
    /**
     * Finds orders that need processing
     */
    public List<Order> findOrdersToProcess() {
        return orderRepository.findOrdersToProcess();
    }
    
    /**
     * Finds orders in date range
     */
    public List<Order> findOrdersInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    // Private validation methods
    
    private void validateCustomerId(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Valid customer ID is required");
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    private void validateOrderCanBeModified(Order order) {
        if (!order.canBeModified()) {
            throw new IllegalStateException("Order cannot be modified in " + order.getStatus() + " status");
        }
    }
    
    private void validateOrderItemsAvailability(Order order) {
        order.getOrderItems().forEach(item -> {
            if (!productService.isProductAvailable(item.getProduct().getId(), item.getQuantity())) {
                throw new InsufficientStockException(
                    String.format("Product %s is no longer available in required quantity",
                                item.getProduct().getName()));
            }
        });
    }
    
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range bounds cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    /**
     * Custom exceptions for order operations
     */
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
