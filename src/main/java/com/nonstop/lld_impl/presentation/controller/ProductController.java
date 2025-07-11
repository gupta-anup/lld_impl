package com.nonstop.lld_impl.presentation.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nonstop.lld_impl.application.service.ProductService;
import com.nonstop.lld_impl.domain.model.Product;
import com.nonstop.lld_impl.domain.model.ProductCategory;
import com.nonstop.lld_impl.presentation.dto.CreateProductRequest;
import com.nonstop.lld_impl.presentation.dto.ProductResponse;
import com.nonstop.lld_impl.presentation.dto.UpdatePriceRequest;

import jakarta.validation.Valid;

/**
 * Product REST Controller
 * 
 * LLD Principles Applied:
 * 1. Controller Pattern: Handles HTTP requests and responses
 * 2. DTO Pattern: Data Transfer Objects for API communication
 * 3. Adapter Pattern: Converts between domain models and DTOs
 * 4. Facade Pattern: Provides simple interface to complex business logic
 * 5. Separation of Concerns: Only handles HTTP/REST concerns
 * 6. Single Responsibility: Only manages product-related endpoints
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Creates a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        try {
            Product product = productService.createProduct(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getCategory()
            );
            
            ProductResponse response = convertToResponse(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets all products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        List<ProductResponse> responses = products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Gets product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findProductById(id);
            ProductResponse response = convertToResponse(product);
            return ResponseEntity.ok(response);
            
        } catch (ProductService.ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Gets products by category
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable ProductCategory category) {
        List<Product> products = productService.findProductsByCategory(category);
        List<ProductResponse> responses = products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Searches products by name
     * GET /api/products/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        List<Product> products = productService.searchProductsByName(name);
        List<ProductResponse> responses = products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Gets available products
     * GET /api/products/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        List<Product> products = productService.findAvailableProducts();
        List<ProductResponse> responses = products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Gets products in price range
     * GET /api/products/price-range?min={min}&max={max}
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsInPriceRange(
            @RequestParam BigDecimal min, 
            @RequestParam BigDecimal max) {
        
        try {
            List<Product> products = productService.findProductsInPriceRange(min, max);
            List<ProductResponse> responses = products.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets low stock products
     * GET /api/products/low-stock?threshold={threshold}
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(@RequestParam Integer threshold) {
        try {
            List<Product> products = productService.findLowStockProducts(threshold);
            List<ProductResponse> responses = products.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Updates product price
     * PUT /api/products/{id}/price
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<ProductResponse> updatePrice(@PathVariable Long id, 
                                                     @Valid @RequestBody UpdatePriceRequest request) {
        try {
            Product product = productService.updatePrice(id, request.getNewPrice());
            ProductResponse response = convertToResponse(product);
            return ResponseEntity.ok(response);
            
        } catch (ProductService.ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Updates product stock
     * PUT /api/products/{id}/stock
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable Long id,
                                                     @RequestParam Integer quantity,
                                                     @RequestParam ProductService.StockOperation operation) {
        try {
            Product product = productService.updateStock(id, quantity, operation);
            ProductResponse response = convertToResponse(product);
            return ResponseEntity.ok(response);
            
        } catch (ProductService.ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Checks if product is available
     * GET /api/products/{id}/availability?quantity={quantity}
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            boolean isAvailable = productService.isProductAvailable(id, quantity);
            return ResponseEntity.ok(isAvailable);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
    
    // Private helper method for DTO conversion
    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getCategory(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
