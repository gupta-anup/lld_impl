package com.nonstop.lld_impl.application.service;

import com.nonstop.lld_impl.domain.model.Product;
import com.nonstop.lld_impl.domain.model.ProductCategory;
import com.nonstop.lld_impl.domain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Service Implementation
 * 
 * LLD Principles Applied:
 * 1. Service Pattern: Encapsulates business logic
 * 2. Facade Pattern: Provides simplified interface to complex subsystems
 * 3. Dependency Injection: Loose coupling with repository
 * 4. Single Responsibility: Only handles product-related business operations
 * 5. Open/Closed Principle: Open for extension, closed for modification
 * 6. Transactional Boundary: Manages database transactions
 */
@Service
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Creates a new product
     * Validates business rules before creation
     */
    @Transactional
    public Product createProduct(String name, String description, BigDecimal price, 
                               Integer stockQuantity, ProductCategory category) {
        
        // Business validation
        validateProductCreation(name, price, stockQuantity);
        
        // Check for duplicate product name
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Product with name '" + name + "' already exists");
        }
        
        Product product = new Product(name, description, price, stockQuantity, category);
        return productRepository.save(product);
    }
    
    /**
     * Updates product price
     * Implements business rule validation
     */
    @Transactional
    public Product updatePrice(Long productId, BigDecimal newPrice) {
        Product product = findProductById(productId);
        product.updatePrice(newPrice);
        return productRepository.save(product);
    }
    
    /**
     * Updates product stock
     * Used by inventory management
     */
    @Transactional
    public Product updateStock(Long productId, Integer quantity, StockOperation operation) {
        Product product = findProductById(productId);
        
        switch (operation) {
            case ADD -> product.increaseStock(quantity);
            case REDUCE -> product.reduceStock(quantity);
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Reserves stock for an order
     * Critical for order processing
     */
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        Product product = findProductById(productId);
        
        if (!product.isAvailable(quantity)) {
            throw new InsufficientStockException(
                String.format("Cannot reserve %d units of %s. Available: %d", 
                            quantity, product.getName(), product.getStockQuantity()));
        }
        
        product.reduceStock(quantity);
        productRepository.save(product);
    }
    
    /**
     * Releases reserved stock (e.g., when order is cancelled)
     */
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        Product product = findProductById(productId);
        product.increaseStock(quantity);
        productRepository.save(product);
    }
    
    /**
     * Finds product by ID
     */
    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
    }
    
    /**
     * Finds all products
     */
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    
    /**
     * Finds products by category
     */
    public List<Product> findProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }
    
    /**
     * Searches products by name
     */
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Finds available products
     */
    public List<Product> findAvailableProducts() {
        return productRepository.findAvailableProducts();
    }
    
    /**
     * Finds products in price range
     */
    public List<Product> findProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    /**
     * Finds low stock products for inventory management
     */
    public List<Product> findLowStockProducts(Integer threshold) {
        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("Stock threshold must be non-negative");
        }
        return productRepository.findLowStockProducts(threshold);
    }
    
    /**
     * Checks if product is available for purchase
     */
    public boolean isProductAvailable(Long productId, Integer quantity) {
        try {
            Product product = findProductById(productId);
            return product.isAvailable(quantity);
        } catch (ProductNotFoundException e) {
            return false;
        }
    }
    
    // Private helper methods for validation
    
    private void validateProductCreation(String name, BigDecimal price, Integer stockQuantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be non-negative");
        }
    }
    
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Price range bounds cannot be null");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price range bounds must be non-negative");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
    }
    
    /**
     * Enum for stock operations
     * Follows Command Pattern for operations
     */
    public enum StockOperation {
        ADD, REDUCE
    }
    
    /**
     * Custom exceptions for better error handling
     * Follows Exception Hierarchy Pattern
     */
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
