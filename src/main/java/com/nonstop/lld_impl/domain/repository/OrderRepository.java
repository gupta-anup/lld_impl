package com.nonstop.lld_impl.domain.repository;

import com.nonstop.lld_impl.domain.model.Order;
import com.nonstop.lld_impl.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Repository Interface
 * 
 * LLD Principles Applied:
 * 1. Repository Pattern: Encapsulates order data access
 * 2. Query Object Pattern: Complex queries as methods
 * 3. Specification Pattern: Flexible query building (can be extended)
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by customer ID
     */
    List<Order> findByCustomerId(Long customerId);
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders by customer and status
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
    
    /**
     * Find orders created between dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find recent orders for a customer
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByCustomer(Long customerId);
    
    /**
     * Find orders that need processing (CONFIRMED status)
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' ORDER BY o.createdAt ASC")
    List<Order> findOrdersToProcess();
    
    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);
    
    /**
     * Find orders with total amount greater than specified value
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    List<Order> findHighValueOrders(java.math.BigDecimal amount);
}
