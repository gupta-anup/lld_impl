package com.nonstop.lld_impl.domain.repository;

import com.nonstop.lld_impl.domain.model.Product;
import com.nonstop.lld_impl.domain.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository Interface
 * 
 * LLD Principles Applied:
 * 1. Repository Pattern: Abstracts data access logic
 * 2. Dependency Inversion: Depends on abstraction, not concrete implementation
 * 3. Single Responsibility: Only handles Product data operations
 * 4. Interface Segregation: Focused interface for Product operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find products by category
     */
    List<Product> findByCategory(ProductCategory category);
    
    /**
     * Find products by name containing (case-insensitive search)
     */
    List<Product> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find products with stock quantity greater than specified amount
     */
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    /**
     * Find products in a price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find available products (stock > 0)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAvailableProducts();
    
    /**
     * Find products by category with minimum stock
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.stockQuantity >= :minStock")
    List<Product> findByCategoryWithMinimumStock(ProductCategory category, Integer minStock);
    
    /**
     * Find low stock products (stock below threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(Integer threshold);
    
    /**
     * Check if product exists by name (for uniqueness validation)
     */
    boolean existsByNameIgnoreCase(String name);
}
