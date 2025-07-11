package com.nonstop.lld_impl.application.service;

import com.nonstop.lld_impl.domain.model.Product;
import com.nonstop.lld_impl.domain.model.ProductCategory;
import com.nonstop.lld_impl.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Product Service Test
 * 
 * LLD Testing Principles:
 * 1. Unit Testing: Tests business logic in isolation
 * 2. Mocking: Uses mocks to isolate dependencies
 * 3. Test-Driven Development: Validates business rules
 * 4. Behavior Testing: Tests expected behaviors, not just state
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product(
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            50,
            ProductCategory.ELECTRONICS
        );
    }
    
    @Test
    @DisplayName("Should create product successfully with valid data")
    void shouldCreateProductSuccessfully() {
        // Given
        String name = "New Product";
        String description = "New Description";
        BigDecimal price = new BigDecimal("199.99");
        Integer stock = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;
        
        when(productRepository.existsByNameIgnoreCase(name)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        Product result = productService.createProduct(name, description, price, stock, category);
        
        // Then
        assertNotNull(result);
        verify(productRepository).existsByNameIgnoreCase(name);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when creating product with duplicate name")
    void shouldThrowExceptionForDuplicateName() {
        // Given
        String duplicateName = "Existing Product";
        when(productRepository.existsByNameIgnoreCase(duplicateName)).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(
                duplicateName,
                "Description",
                new BigDecimal("99.99"),
                50,
                ProductCategory.ELECTRONICS
            )
        );
        
        assertEquals("Product with name 'Existing Product' already exists", exception.getMessage());
        verify(productRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when creating product with null name")
    void shouldThrowExceptionForNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(
                null,
                "Description",
                new BigDecimal("99.99"),
                50,
                ProductCategory.ELECTRONICS
            )
        );
        
        assertEquals("Product name is required", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when creating product with negative price")
    void shouldThrowExceptionForNegativePrice() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(
                "Product",
                "Description",
                new BigDecimal("-10.00"),
                50,
                ProductCategory.ELECTRONICS
            )
        );
        
        assertEquals("Product price must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should update product price successfully")
    void shouldUpdatePriceSuccessfully() {
        // Given
        Long productId = 1L;
        BigDecimal newPrice = new BigDecimal("149.99");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        
        // When
        Product result = productService.updatePrice(productId, newPrice);
        
        // Then
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(testProduct);
    }
    
    @Test
    @DisplayName("Should reserve stock successfully when available")
    void shouldReserveStockSuccessfully() {
        // Given
        Long productId = 1L;
        Integer quantityToReserve = 10;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        
        // When
        productService.reserveStock(productId, quantityToReserve);
        
        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(testProduct);
    }
    
    @Test
    @DisplayName("Should throw exception when reserving more stock than available")
    void shouldThrowExceptionForInsufficientStock() {
        // Given
        Long productId = 1L;
        Integer quantityToReserve = 100; // More than available (50)
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        
        // When & Then
        ProductService.InsufficientStockException exception = assertThrows(
            ProductService.InsufficientStockException.class,
            () -> productService.reserveStock(productId, quantityToReserve)
        );
        
        assertTrue(exception.getMessage().contains("Cannot reserve"));
        verify(productRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        ProductService.ProductNotFoundException exception = assertThrows(
            ProductService.ProductNotFoundException.class,
            () -> productService.findProductById(nonExistentId)
        );
        
        assertEquals("Product not found with ID: 999", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should check product availability correctly")
    void shouldCheckProductAvailabilityCorrectly() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        
        // When & Then
        assertTrue(productService.isProductAvailable(productId, 10)); // Available
        assertTrue(productService.isProductAvailable(productId, 50)); // Exact stock
        assertFalse(productService.isProductAvailable(productId, 51)); // Exceeds stock
        assertFalse(productService.isProductAvailable(productId, 0)); // Invalid quantity
    }
}
