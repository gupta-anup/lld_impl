# 📦 Phase 1: E-Commerce Order Management System

## 🎯 **Learning Objectives**

This foundational phase introduces **core design patterns** and **SOLID principles** through building a comprehensive e-commerce platform. You'll establish a solid foundation in Low Level Design (LLD) by implementing clean architecture, domain-driven design, and essential patterns used in production systems.

---

## 🏗️ **System Overview**

### **What We're Building**
A production-level **E-Commerce Order Management System** similar to Amazon, Flipkart, or Shopify that handles:
- 🛒 **Product Catalog**: Comprehensive product management with categories
- 📦 **Order Processing**: Complete order lifecycle management
- 📊 **Inventory Management**: Stock tracking and reservation
- 💰 **Pricing & Discounts**: Flexible pricing strategies
- 👥 **User Management**: Customer profiles and authentication
- 🔍 **Search & Filtering**: Product discovery capabilities
- 📈 **Analytics**: Order and product insights

### **Architecture Highlights**
- **Clean Architecture**: Clear separation of concerns across layers
- **Domain-Driven Design**: Rich domain models with business logic
- **SOLID Principles**: All five principles demonstrated in practice
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic organization
- **RESTful APIs**: Well-designed HTTP endpoints

---

## 🎨 **Design Patterns to Master**

### **1. Repository Pattern** 🗄️
**Purpose**: Abstract data access logic and provide a clean interface for domain operations

**Implementation**:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Custom query methods
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
}
```

**Real-world Usage**:
- Clean separation between business logic and data access
- Easy testing with mock repositories
- Database technology independence
- Centralized query logic

---

### **2. Service Layer Pattern** ⚙️
**Purpose**: Encapsulate business logic and coordinate between different domain objects

**Implementation**:
```java
@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductValidator productValidator;
    
    public Product createProduct(CreateProductRequest request) {
        // Validate business rules
        productValidator.validateCreateRequest(request);
        
        // Create domain object
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .category(request.getCategory())
            .stockQuantity(request.getStockQuantity())
            .build();
        
        // Save and return
        return productRepository.save(product);
    }
    
    public void reserveStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        if (!product.isAvailable(quantity)) {
            throw new InsufficientStockException(
                "Not enough stock available for product: " + productId
            );
        }
        
        product.reduceStock(quantity);
        productRepository.save(product);
    }
}
```

**Real-world Usage**:
- Transaction management
- Business rule enforcement
- Cross-cutting concerns (logging, security)
- Orchestration of multiple domain objects

---

### **3. Domain-Driven Design (DDD)** 🏛️
**Purpose**: Create rich domain models that encapsulate business logic and rules

**Implementation**:
```java
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    
    // Business logic methods
    public boolean isAvailable() {
        return stockQuantity > 0;
    }
    
    public boolean isAvailable(Integer requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    public void reduceStock(Integer quantity) {
        if (!isAvailable(quantity)) {
            throw new InsufficientStockException(
                "Cannot reduce stock below zero"
            );
        }
        this.stockQuantity -= quantity;
    }
    
    public void increaseStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Stock increase must be positive"
            );
        }
        this.stockQuantity += quantity;
    }
    
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Price must be positive"
            );
        }
        this.price = newPrice;
    }
}
```

**Real-world Usage**:
- Business logic close to data
- Self-validating domain objects
- Expressive business operations
- Reduced anemic domain models

---

### **4. DTO Pattern** 📋
**Purpose**: Transfer data between layers while maintaining clean boundaries

**Implementation**:
```java
// Request DTOs
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Category is required")
    private ProductCategory category;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    // Getters, setters, validation
}

// Response DTOs
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private Integer stockQuantity;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Factory method for conversion
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.getCategory())
            .stockQuantity(product.getStockQuantity())
            .available(product.isAvailable())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}
```

**Real-world Usage**:
- Input validation at API boundary
- Hide internal domain complexity
- API versioning support
- Security through data hiding

---

### **5. State Pattern** 🔄
**Purpose**: Manage complex state transitions in order processing

**Implementation**:
```java
public enum OrderStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == CONFIRMED || newStatus == CANCELLED;
        }
    },
    CONFIRMED {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == PROCESSING || newStatus == CANCELLED;
        }
    },
    PROCESSING {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == SHIPPED || newStatus == CANCELLED;
        }
    },
    SHIPPED {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DELIVERED;
        }
    },
    DELIVERED {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false; // Terminal state
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false; // Terminal state
        }
    };
    
    public abstract boolean canTransitionTo(OrderStatus newStatus);
}

@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    public void updateStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(
                String.format("Cannot transition from %s to %s", 
                    status, newStatus)
            );
        }
        this.status = newStatus;
    }
}
```

**Real-world Usage**:
- Complex business workflows
- State machine implementation
- Business rule enforcement
- Audit trail maintenance

---

### **6. Factory Pattern** 🏭
**Purpose**: Create objects with complex initialization logic

**Implementation**:
```java
@Component
public class OrderFactory {
    
    private final ProductService productService;
    private final UserService userService;
    private final PricingService pricingService;
    
    public Order createOrder(CreateOrderRequest request) {
        // Validate user
        User user = userService.getActiveUser(request.getUserId());
        
        // Validate and prepare order items
        List<OrderItem> orderItems = createOrderItems(request.getItems());
        
        // Calculate totals
        Money subtotal = calculateSubtotal(orderItems);
        Money tax = pricingService.calculateTax(subtotal, user.getAddress());
        Money shipping = pricingService.calculateShipping(orderItems, user.getAddress());
        Money total = subtotal.add(tax).add(shipping);
        
        // Create order
        return Order.builder()
            .userId(user.getId())
            .items(orderItems)
            .subtotal(subtotal)
            .tax(tax)
            .shipping(shipping)
            .total(total)
            .status(OrderStatus.PENDING)
            .shippingAddress(request.getShippingAddress())
            .billingAddress(request.getBillingAddress())
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    private List<OrderItem> createOrderItems(List<CreateOrderItemRequest> requests) {
        return requests.stream()
            .map(this::createOrderItem)
            .collect(Collectors.toList());
    }
    
    private OrderItem createOrderItem(CreateOrderItemRequest request) {
        Product product = productService.getProduct(request.getProductId());
        
        if (!product.isAvailable(request.getQuantity())) {
            throw new InsufficientStockException(
                "Product not available: " + product.getName()
            );
        }
        
        return OrderItem.builder()
            .productId(product.getId())
            .productName(product.getName())
            .unitPrice(product.getPrice())
            .quantity(request.getQuantity())
            .subtotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
            .build();
    }
}
```

**Real-world Usage**:
- Complex object creation
- Multiple creation strategies
- Encapsulation of creation logic
- Dependency injection coordination

---

## 🏛️ **SOLID Principles Implementation**

### **S - Single Responsibility Principle**
Each class has one reason to change:
- `Product`: Manages product data and business rules
- `ProductRepository`: Handles data persistence
- `ProductService`: Orchestrates business operations
- `ProductController`: Handles HTTP requests/responses

### **O - Open/Closed Principle**
Classes are open for extension, closed for modification:
- `PricingStrategy` interface with different implementations
- `OrderValidator` with pluggable validation rules

### **L - Liskov Substitution Principle**
Derived classes are substitutable for base classes:
- Different repository implementations
- Various pricing strategy implementations

### **I - Interface Segregation Principle**
Clients depend only on interfaces they use:
- Separate read and write repository interfaces
- Specific service interfaces for different operations

### **D - Dependency Inversion Principle**
High-level modules don't depend on low-level modules:
- Services depend on repository interfaces, not implementations
- Controllers depend on service interfaces

---

## 🚀 **Key Features Implemented**

### **🛒 Product Management**
- CRUD operations for products
- Category-based organization
- Stock management with real-time updates
- Price management and validation
- Product search and filtering

### **📦 Order Processing**
- Complete order lifecycle management
- Order item management with pricing
- Status tracking with state transitions
- Order validation and business rules
- Tax and shipping calculations

### **👥 User Management**
- User registration and authentication
- Profile management
- Address management for shipping/billing
- Order history tracking

### **📊 Business Logic**
- Inventory reservation and release
- Price calculations with tax and shipping
- Stock availability checking
- Order validation rules
- Business rule enforcement

### **🔍 Search & Analytics**
- Product search by name, category, price range
- Low stock alerts and reporting
- Order statistics and insights
- User behavior tracking

---

## 🧪 **Testing Strategy**

### **Unit Testing**
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductValidator productValidator;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .category(ProductCategory.ELECTRONICS)
            .stockQuantity(10)
            .build();
        
        Product savedProduct = Product.builder()
            .id(1L)
            .name(request.getName())
            .price(request.getPrice())
            .category(request.getCategory())
            .stockQuantity(request.getStockQuantity())
            .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // When
        Product result = productService.createProduct(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productValidator).validateCreateRequest(request);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        Product product = Product.builder()
            .id(1L)
            .stockQuantity(5)
            .build();
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When & Then
        assertThatThrownBy(() -> productService.reserveStock(1L, 10))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Not enough stock available");
    }
}
```

**Testing Coverage**:
- ✅ **Unit Tests**: Service layer business logic
- ✅ **Integration Tests**: Repository layer with database
- ✅ **Controller Tests**: REST API endpoints
- ✅ **Domain Tests**: Business rule validation

---

## 📚 **Learning Progression**

### **Week 1: Foundation & Setup**
- [ ] Project setup and understanding Spring Boot structure
- [ ] Implement basic domain models (Product, User)
- [ ] Create repository interfaces and basic CRUD
- [ ] Understand dependency injection and Spring annotations

### **Week 2: Business Logic & Patterns**
- [ ] Implement service layer with business logic
- [ ] Add comprehensive validation and error handling
- [ ] Create DTOs for clean API contracts
- [ ] Implement factory pattern for complex object creation

### **Week 3: Advanced Features & Testing**
- [ ] Complete order management with state transitions
- [ ] Add search and filtering capabilities
- [ ] Implement comprehensive test suite
- [ ] Add logging and monitoring

### **Week 4: Polish & Documentation**
- [ ] API documentation and examples
- [ ] Performance optimization
- [ ] Code review and refactoring
- [ ] Prepare for Phase 2 transition

---

## 🎯 **Success Criteria**

After completing Phase 1, you should be able to:

### **Technical Mastery**
- ✅ Implement clean architecture with proper layer separation
- ✅ Use core design patterns (Repository, Service Layer, DTO, Factory, State)
- ✅ Apply all SOLID principles in practice
- ✅ Write comprehensive unit and integration tests
- ✅ Create well-designed RESTful APIs

### **Real-world Skills**
- ✅ Build production-ready Spring Boot applications
- ✅ Implement domain-driven design principles
- ✅ Handle complex business logic and validation
- ✅ Manage database operations with JPA/Hibernate
- ✅ Create maintainable and testable code

### **Interview Preparation**
- ✅ Explain core design patterns with concrete examples
- ✅ Discuss SOLID principles and their benefits
- ✅ Design simple system architectures
- ✅ Write clean, readable code under pressure
- ✅ Demonstrate testing best practices

---

## 🚀 **Getting Started**

### **Prerequisites**
- Basic Java knowledge (OOP concepts)
- Understanding of Spring Framework basics
- Familiarity with Maven/Gradle
- Basic SQL knowledge

### **Setup Instructions**
```bash
# Ensure you're on Phase 1 branch
git checkout phase/1/e-commerce

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Access the application
# API: http://localhost:8080
# Database Console: http://localhost:8080/h2-console
# API Documentation: http://localhost:8080/swagger-ui.html
```

### **Technology Stack**
- **Framework**: Spring Boot 3.x
- **Database**: H2 (in-memory for development)
- **ORM**: JPA/Hibernate
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven
- **Documentation**: SpringDoc OpenAPI

### **Exploring the Code**
```bash
# Key directories to explore
src/main/java/com/nonstop/lld_impl/
├── domain/          # Domain models and business logic
├── repository/      # Data access layer
├── service/         # Business service layer
├── controller/      # REST API controllers
├── dto/            # Data transfer objects
└── config/         # Configuration classes

src/test/java/       # Test cases
├── service/        # Service layer tests
├── repository/     # Repository tests
└── controller/     # API tests
```

---

## 📋 **API Endpoints Overview**

### **Product Management**
```bash
# Get all products
GET /api/products

# Get product by ID
GET /api/products/{id}

# Create new product
POST /api/products

# Update product
PUT /api/products/{id}

# Delete product
DELETE /api/products/{id}

# Search products
GET /api/products/search?name=laptop&category=ELECTRONICS

# Check stock
GET /api/products/{id}/stock
```

### **Order Management**
```bash
# Create order
POST /api/orders

# Get order by ID
GET /api/orders/{id}

# Update order status
PUT /api/orders/{id}/status

# Get user orders
GET /api/orders/user/{userId}

# Cancel order
DELETE /api/orders/{id}
```

---

## 🎉 **What's Next?**

After mastering Phase 1, you'll be ready for:

### **🎟️ Phase 2: Event Booking System**
- Advanced behavioral patterns (Observer, Command, Strategy)
- Real-time systems and event-driven architecture
- Complex state management and concurrency

### **💬 Phase 3: Chat Application**
- Publisher-Subscriber patterns
- WebSocket integration for real-time communication
- Microservices architecture

### **🔧 Phase 4: Enterprise Patterns**
- CQRS and Event Sourcing
- Distributed systems patterns
- Microservices and cloud-native design

---

## 📞 **Support**

- **Documentation**: `LLD_LEARNING_GUIDE.md` - Comprehensive learning guide
- **Mastery Summary**: `LLD_MASTERY_SUMMARY.md` - Key concepts summary
- **Code Comments**: Detailed explanations throughout the codebase
- **Test Cases**: Examples and expected behavior demonstrations

## 🎊 **Congratulations!**

You're building a solid foundation in Low Level Design! This phase establishes the core patterns and principles that will serve you throughout your career. The skills learned here are fundamental to all software development and will prepare you for increasingly complex systems.

**Ready to build your first production-quality system? Let's code! 🚀💻**
