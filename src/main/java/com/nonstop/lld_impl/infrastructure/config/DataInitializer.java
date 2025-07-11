package com.nonstop.lld_impl.infrastructure.config;

import com.nonstop.lld_impl.application.service.ProductService;
import com.nonstop.lld_impl.domain.model.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Data Initializer
 * 
 * LLD Principles Applied:
 * 1. Builder Pattern: Constructs test data systematically
 * 2. Factory Pattern: Creates various product types
 * 3. Command Pattern: Initialization as a command
 * 4. Separation of Concerns: Only handles data initialization
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductService productService;
    
    @Autowired
    public DataInitializer(ProductService productService) {
        this.productService = productService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🚀 Initializing LLD Demo Data...");
        
        createElectronicsProducts();
        createClothingProducts();
        createBooksProducts();
        createSportsProducts();
        
        System.out.println("✅ Demo data initialized successfully!\n");
        printWelcomeMessage();
    }
    
    /**
     * Creates electronics products demonstrating different patterns
     */
    private void createElectronicsProducts() {
        System.out.println("📱 Creating Electronics products...");
        
        // High-value, low-stock item (demonstrates inventory management)
        productService.createProduct(
            "iPhone 15 Pro Max",
            "Latest Apple smartphone with A17 Pro chip",
            new BigDecimal("1199.99"),
            15,
            ProductCategory.ELECTRONICS
        );
        
        // Popular item with good stock
        productService.createProduct(
            "Samsung Galaxy S24",
            "Latest Samsung flagship smartphone",
            new BigDecimal("899.99"),
            50,
            ProductCategory.ELECTRONICS
        );
        
        // Budget option with high stock
        productService.createProduct(
            "Wireless Earbuds",
            "Bluetooth 5.0 wireless earbuds with noise cancellation",
            new BigDecimal("89.99"),
            100,
            ProductCategory.ELECTRONICS
        );
    }
    
    /**
     * Creates clothing products with various price points
     */
    private void createClothingProducts() {
        System.out.println("👕 Creating Clothing products...");
        
        productService.createProduct(
            "Premium Cotton T-Shirt",
            "100% organic cotton, comfortable fit",
            new BigDecimal("29.99"),
            200,
            ProductCategory.CLOTHING
        );
        
        productService.createProduct(
            "Designer Jeans",
            "Premium denim with modern fit",
            new BigDecimal("89.99"),
            75,
            ProductCategory.CLOTHING
        );
        
        productService.createProduct(
            "Winter Jacket",
            "Insulated winter jacket for extreme weather",
            new BigDecimal("149.99"),
            30,
            ProductCategory.CLOTHING
        );
    }
    
    /**
     * Creates book products with different genres
     */
    private void createBooksProducts() {
        System.out.println("📚 Creating Books...");
        
        productService.createProduct(
            "Clean Code",
            "A Handbook of Agile Software Craftsmanship by Robert C. Martin",
            new BigDecimal("42.99"),
            60,
            ProductCategory.BOOKS
        );
        
        productService.createProduct(
            "Design Patterns",
            "Elements of Reusable Object-Oriented Software",
            new BigDecimal("54.99"),
            45,
            ProductCategory.BOOKS
        );
        
        productService.createProduct(
            "System Design Interview",
            "An insider's guide to system design interviews",
            new BigDecimal("39.99"),
            80,
            ProductCategory.BOOKS
        );
    }
    
    /**
     * Creates sports products
     */
    private void createSportsProducts() {
        System.out.println("⚽ Creating Sports products...");
        
        productService.createProduct(
            "Professional Football",
            "Official size and weight football",
            new BigDecimal("24.99"),
            120,
            ProductCategory.SPORTS
        );
        
        productService.createProduct(
            "Yoga Mat",
            "Non-slip exercise yoga mat",
            new BigDecimal("34.99"),
            150,
            ProductCategory.SPORTS
        );
    }
    
    private void printWelcomeMessage() {
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("🎓 LLD (Low Level Design) Learning Platform - Ready!");
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("📋 Available Endpoints:");
        System.out.println();
        System.out.println("🛍️  PRODUCT ENDPOINTS:");
        System.out.println("   GET    /api/products              - Get all products");
        System.out.println("   GET    /api/products/{id}         - Get product by ID");
        System.out.println("   GET    /api/products/available    - Get available products");
        System.out.println("   GET    /api/products/category/{category} - Get by category");
        System.out.println("   POST   /api/products              - Create new product");
        System.out.println("   PUT    /api/products/{id}/price   - Update product price");
        System.out.println();
        System.out.println("📦 ORDER ENDPOINTS:");
        System.out.println("   POST   /api/orders                - Create new order");
        System.out.println("   GET    /api/orders/{id}           - Get order by ID");
        System.out.println("   POST   /api/orders/{id}/items     - Add item to order");
        System.out.println("   PUT    /api/orders/{id}/confirm   - Confirm order");
        System.out.println("   PUT    /api/orders/{id}/process   - Process order");
        System.out.println("   PUT    /api/orders/{id}/cancel    - Cancel order");
        System.out.println();
        System.out.println("🔍 DATABASE CONSOLE:");
        System.out.println("   URL: http://localhost:8080/h2-console");
        System.out.println("   JDBC URL: jdbc:h2:mem:lld_impl_db");
        System.out.println("   Username: sa");
        System.out.println("   Password: password");
        System.out.println();
        System.out.println("🎯 LLD PATTERNS IMPLEMENTED:");
        System.out.println("   ✅ Domain-Driven Design (DDD)");
        System.out.println("   ✅ Repository Pattern");
        System.out.println("   ✅ Service Layer Pattern");
        System.out.println("   ✅ DTO Pattern");
        System.out.println("   ✅ Factory Pattern");
        System.out.println("   ✅ Strategy Pattern");
        System.out.println("   ✅ State Pattern (Order Status)");
        System.out.println("   ✅ Command Pattern");
        System.out.println("   ✅ Facade Pattern");
        System.out.println("   ✅ SOLID Principles");
        System.out.println();
        System.out.println("🚀 Application started successfully on http://localhost:8080");
        System.out.println("════════════════════════════════════════════════════════════");
    }
}
