package com.nonstop.lld_impl.presentation.controller;

import com.nonstop.lld_impl.application.service.OrderService;
import com.nonstop.lld_impl.domain.model.Order;
import com.nonstop.lld_impl.domain.model.OrderStatus;
import com.nonstop.lld_impl.presentation.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order REST Controller
 * 
 * LLD Principles Applied:
 * 1. Controller Pattern: Handles order-related HTTP requests
 * 2. Command Pattern: Order operations as commands
 * 3. State Machine Pattern: Order status transitions
 * 4. Facade Pattern: Simplifies complex order operations
 * 5. DTO Pattern: Clean data transfer
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Creates a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getCustomerId());
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.findOrderById(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Gets orders by customer
     * GET /api/orders/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            List<Order> orders = orderService.findOrdersByCustomer(customerId);
            List<OrderResponse> responses = orders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets orders by status
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.findOrdersByStatus(status);
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Gets orders to process
     * GET /api/orders/to-process
     */
    @GetMapping("/to-process")
    public ResponseEntity<List<OrderResponse>> getOrdersToProcess() {
        List<Order> orders = orderService.findOrdersToProcess();
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Gets orders in date range
     * GET /api/orders/date-range?start={start}&end={end}
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getOrdersInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            List<Order> orders = orderService.findOrdersInDateRange(start, end);
            List<OrderResponse> responses = orders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Adds item to order
     * POST /api/orders/{id}/items
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponse> addItemToOrder(@PathVariable Long id,
                                                      @Valid @RequestBody AddItemRequest request) {
        try {
            Order order = orderService.addItemToOrder(id, request.getProductId(), request.getQuantity());
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException | 
                 OrderService.InsufficientStockException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Removes item from order
     * DELETE /api/orders/{id}/items/{productId}
     */
    @DeleteMapping("/{id}/items/{productId}")
    public ResponseEntity<OrderResponse> removeItemFromOrder(@PathVariable Long id,
                                                           @PathVariable Long productId) {
        try {
            Order order = orderService.removeItemFromOrder(id, productId);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Confirms an order
     * PUT /api/orders/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        try {
            Order order = orderService.confirmOrder(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | OrderService.InsufficientStockException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Processes an order
     * PUT /api/orders/{id}/process
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<OrderResponse> processOrder(@PathVariable Long id) {
        try {
            Order order = orderService.processOrder(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Ships an order
     * PUT /api/orders/{id}/ship
     */
    @PutMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable Long id) {
        try {
            Order order = orderService.shipOrder(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Delivers an order
     * PUT /api/orders/{id}/deliver
     */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id) {
        try {
            Order order = orderService.deliverOrder(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Cancels an order
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
            
        } catch (OrderService.OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Private helper method for DTO conversion
    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
                ))
                .collect(Collectors.toList());
        
        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items
        );
    }
}
