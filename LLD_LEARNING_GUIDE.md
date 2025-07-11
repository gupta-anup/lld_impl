# Low Level Design (LLD) Learning Guide - E-Commerce System

## 🎯 **Overview**

This project demonstrates **production-level Low Level Design (LLD)** principles through a comprehensive **E-Commerce Order Management System**. As an SDE 1, you'll learn how to design scalable, maintainable, and robust systems using industry-standard patterns and practices.

---

## 📚 **LLD Principles Implemented**

### 1. **SOLID Principles**

#### **Single Responsibility Principle (SRP)**
- **`Product` class**: Only handles product-related data and behavior
- **`ProductService`**: Only manages product business logic
- **`ProductController`**: Only handles HTTP requests/responses

```java
// ✅ Good: Single responsibility
public class Product {
    // Only product-related behavior
    public boolean isAvailable(int quantity) { /* ... */ }
    public void reduceStock(int quantity) { /* ... */ }
}

// ✅ Good: Single responsibility
public class ProductService {
    // Only product business logic
    public Product createProduct(...) { /* ... */ }
    public void reserveStock(...) { /* ... */ }
}
```

#### **Open/Closed Principle (OCP)**
- **Services are open for extension, closed for modification**
- **Strategy Pattern** in order processing allows new payment methods
- **Factory Pattern** allows adding new product types

#### **Liskov Substitution Principle (LSP)**
- **Repository interfaces** can be substituted with different implementations
- **Service abstractions** allow different business rule implementations

#### **Interface Segregation Principle (ISP)**
- **Focused repository interfaces** with specific methods
- **DTOs** contain only relevant data for specific operations

#### **Dependency Inversion Principle (DIP)**
- **Services depend on repository abstractions**, not concrete implementations
- **Controllers depend on service interfaces**, not concrete classes

### 2. **Domain-Driven Design (DDD)**

#### **Rich Domain Models**
```java
public class Order {
    // Domain behavior encapsulated
    public void addItem(Product product, int quantity) {
        validateOrderCanBeModified();
        validateProductAndQuantity(product, quantity);
        // Business logic here
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm PENDING orders");
        }
        // State transition logic
    }
}
```

#### **Value Objects**
```java
public enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED;
    
    // Business rules for state transitions
    public boolean canTransitionTo(OrderStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == PROCESSING || targetStatus == CANCELLED;
            // ... more transitions
        };
    }
}
```

#### **Aggregates**
- **Order** is an aggregate root managing **OrderItems**
- **Product** is a standalone aggregate
- Aggregate boundaries ensure consistency

### 3. **Design Patterns**

#### **Repository Pattern**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    // Query methods for specific business needs
}
```

**Benefits:**
- **Separation of concerns**: Data access logic separated from business logic
- **Testability**: Easy to mock for unit tests
- **Flexibility**: Can switch data sources without changing business logic

#### **Service Layer Pattern**
```java
@Service
@Transactional(readOnly = true)
public class OrderService {
    
    @Transactional
    public Order createOrder(Long customerId) {
        // Business validation and logic
        // Transaction management
        // Domain operations
    }
}
```

**Benefits:**
- **Transaction management**: Declarative transaction boundaries
- **Business logic encapsulation**: All business rules in one place
- **Reusability**: Services can be used by multiple controllers

#### **Data Transfer Object (DTO) Pattern**
```java
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    // Validation and transfer logic only
}
```

**Benefits:**
- **Input validation**: Validation at API boundaries
- **Decoupling**: API contracts independent of domain models
- **Versioning**: Different API versions without changing domain

#### **Factory Pattern**
```java
// Used in DataInitializer for creating different product types
private void createElectronicsProducts() {
    productService.createProduct("iPhone 15 Pro Max", ..., ELECTRONICS);
    productService.createProduct("Samsung Galaxy S24", ..., ELECTRONICS);
}
```

#### **Strategy Pattern**
```java
public enum StockOperation {
    ADD, REDUCE;
    // Different strategies for stock operations
}

public Product updateStock(Long productId, Integer quantity, StockOperation operation) {
    switch (operation) {
        case ADD -> product.increaseStock(quantity);
        case REDUCE -> product.reduceStock(quantity);
    }
}
```

#### **State Pattern**
```java
public class Order {
    public void confirm() { /* Transition from PENDING to CONFIRMED */ }
    public void process() { /* Transition from CONFIRMED to PROCESSING */ }
    public void ship() { /* Transition from PROCESSING to SHIPPED */ }
    public void deliver() { /* Transition from SHIPPED to DELIVERED */ }
    public void cancel() { /* Transition to CANCELLED with business rules */ }
}
```

#### **Command Pattern**
```java
// DTOs represent commands
public class AddItemRequest {
    private Long productId;
    private Integer quantity;
    // Represents a command to add item to order
}

// Services execute commands
public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
    // Command execution with business validation
}
```

#### **Facade Pattern**
```java
@RestController
public class ProductController {
    // Provides simplified interface to complex business operations
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        // Facades complex domain operations
        Product product = productService.createProduct(...);
        return ResponseEntity.ok(convertToResponse(product));
    }
}
```

---

## 🏗️ **Architecture Layers**

### **1. Presentation Layer** (`presentation/controller`)
- **Responsibility**: Handle HTTP requests/responses
- **Components**: REST Controllers, DTOs
- **Concerns**: Input validation, response formatting, HTTP status codes

### **2. Application Layer** (`application/service`)
- **Responsibility**: Business logic orchestration
- **Components**: Service classes
- **Concerns**: Transaction management, business rules, domain coordination

### **3. Domain Layer** (`domain/model`)
- **Responsibility**: Core business logic and rules
- **Components**: Entities, Value Objects, Domain Services
- **Concerns**: Business invariants, domain behavior

### **4. Infrastructure Layer** (`domain/repository`)
- **Responsibility**: Data persistence and external services
- **Components**: Repository interfaces and implementations
- **Concerns**: Data access, external API integration

---

## 🔄 **Real-World Business Flows**

### **Product Management Flow**
1. **Create Product**: Validation → Duplicate check → Domain creation → Persistence
2. **Stock Management**: Availability check → Business rules → Stock update → Audit
3. **Price Update**: Authorization → Validation → Domain update → Event publishing

### **Order Processing Flow**
1. **Create Order**: Customer validation → Order creation → Status: PENDING
2. **Add Items**: Stock validation → Inventory reservation → Order update
3. **Confirm Order**: Final validation → Stock confirmation → Status: CONFIRMED
4. **Process Order**: Payment processing → Status: PROCESSING
5. **Ship Order**: Logistics integration → Status: SHIPPED
6. **Deliver Order**: Delivery confirmation → Status: DELIVERED

### **Error Handling Flow**
- **Domain Exceptions**: Business rule violations
- **Service Exceptions**: Orchestration failures
- **Controller Exceptions**: HTTP-specific errors
- **Custom Exceptions**: Domain-specific error types

---

## 🧪 **Testing Strategy**

### **Unit Tests** (Implemented)
- **Domain Logic Testing**: Business rules validation
- **Service Layer Testing**: Mock dependencies, test business flows
- **Repository Testing**: Data access patterns

### **Integration Tests** (Recommended)
- **API Testing**: End-to-end request/response validation
- **Database Testing**: Persistence and query validation
- **Service Integration**: Cross-service communication

### **Production Readiness** (Industry Standards)
- **Monitoring**: Application metrics, health checks
- **Logging**: Structured logging with correlation IDs
- **Security**: Authentication, authorization, input sanitization
- **Performance**: Caching, optimization, load testing

---

## 📊 **Database Design**

### **Tables Created**
```sql
-- Products table with business constraints
CREATE TABLE products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    category ENUM(...) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Orders table with status management
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', ...) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Order items with referential integrity
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL
);
```

---

## 🚀 **API Usage Examples**

### **Product Operations**

#### Create a New Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "stockQuantity": 25,
    "category": "ELECTRONICS"
  }'
```

#### Get All Products
```bash
curl http://localhost:8080/api/products
```

#### Get Products by Category
```bash
curl http://localhost:8080/api/products/category/ELECTRONICS
```

#### Update Product Price
```bash
curl -X PUT http://localhost:8080/api/products/1/price \
  -H "Content-Type: application/json" \
  -d '{"newPrice": 1199.99}'
```

### **Order Operations**

#### Create a New Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 123}'
```

#### Add Item to Order
```bash
curl -X POST http://localhost:8080/api/orders/1/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

#### Confirm Order
```bash
curl -X PUT http://localhost:8080/api/orders/1/confirm
```

#### Process Order
```bash
curl -X PUT http://localhost:8080/api/orders/1/process
```

---

## 🎓 **Learning Progression for SDE 1**

### **Phase 1: Foundation** ✅ (Completed)
- **Domain Modeling**: Rich domain objects with business logic
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic organization
- **REST APIs**: HTTP endpoint design

### **Phase 2: Advanced Patterns** (Next Steps)
- **Event-Driven Architecture**: Domain events, event sourcing
- **CQRS**: Command Query Responsibility Segregation
- **Saga Pattern**: Distributed transaction management
- **Circuit Breaker**: Resilience patterns

### **Phase 3: Scalability** (Future Learning)
- **Microservices**: Service decomposition
- **Message Queues**: Asynchronous processing
- **Caching**: Redis, distributed caching
- **Database Optimization**: Indexing, partitioning

### **Phase 4: Production Excellence** (Senior Level)
- **Monitoring & Observability**: Metrics, tracing, logging
- **Security**: OAuth, JWT, encryption
- **Performance**: Load testing, optimization
- **DevOps**: CI/CD, containerization

---

## 💡 **Key Takeaways for SDE 1**

1. **Business Logic in Domain**: Keep business rules in domain objects, not services
2. **Separation of Concerns**: Each layer has distinct responsibilities
3. **Validation at Boundaries**: Validate input at API layer, not in domain
4. **Transaction Management**: Use declarative transactions in service layer
5. **Error Handling**: Create domain-specific exceptions for better debugging
6. **Testing**: Write unit tests for business logic, integration tests for flows
7. **API Design**: Use DTOs for clean API contracts
8. **Database Design**: Model relationships and constraints properly

---

## 🔧 **Development Environment**

### **Database Console**
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:lld_impl_db`
- **Username**: `sa`
- **Password**: `password`

### **Application URLs**
- **Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health (if actuator added)
- **API Documentation**: http://localhost:8080/swagger-ui.html (if swagger added)

---

## 📝 **Next Steps for Learning**

1. **Extend the System**: Add customer management, payment processing
2. **Add Caching**: Implement Redis for product catalog
3. **Event Sourcing**: Track all state changes as events
4. **API Documentation**: Add OpenAPI/Swagger documentation
5. **Security**: Implement JWT authentication
6. **Monitoring**: Add application metrics and health checks
7. **Testing**: Write comprehensive integration tests
8. **Performance**: Add database indexing and query optimization

This project provides a solid foundation for understanding production-level LLD concepts. Practice by extending the features and implementing additional patterns!
