// OrderController.java - IMPROVED VERSION  
package com.bookstore.controller;

import com.bookstore.dto.OrderRequest;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderStatus;
import com.bookstore.entity.User;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, Authentication auth) {
        try {
            logger.info("Order creation request received");
            logger.debug("Authentication object: {}", auth);
            logger.debug("Authentication name: {}", auth != null ? auth.getName() : "null");
            logger.debug("Authentication principal: {}", auth != null ? auth.getPrincipal() : "null");
            
            if (auth == null) {
                logger.error("No authentication found in security context");
                return ResponseEntity.status(401).body("Authentication required");
            }
            
            String username = auth.getName();
            if (username == null || username.isEmpty()) {
                logger.error("Username is null or empty from authentication");
                return ResponseEntity.status(401).body("Invalid authentication");
            }
            
            if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
                logger.warn("Invalid order request - missing items");
                return ResponseEntity.badRequest().body("Order request and items are required");
            }
            
            logger.debug("Looking up user by username: {}", username);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found in database for username: {}", username);
                        return new RuntimeException("User not found: " + username);
                    });
            
            logger.info("Creating order for user: {} with {} items", user.getUsername(), request.getItems().size());
            Order order = orderService.createOrder(user, request.getItems(), request.getShippingAddress());
            
            logger.info("Order created successfully with ID: {}", order.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create order: " + e.getMessage());
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(Authentication auth) {
        try {
            if (auth == null) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            
            String username = auth.getName();
            logger.debug("Fetching orders for user: {}", username);
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            List<Order> orders = orderService.getUserOrders(user);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Failed to fetch user orders: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to fetch orders: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin")
    public ResponseEntity<?> getAllOrders(Authentication auth) {
        try {
            if (auth == null) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            
            // Check if user has admin role
            if (!auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).body("Admin access required");
            }
            
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Failed to fetch all orders: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to fetch orders: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication auth) {
        try {
            if (auth == null) {
                return ResponseEntity.status(401).body("Authentication required");
            }
            
            // Check if user has admin role
            if (!auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).body("Admin access required");
            }
            
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest().body("Status is required");
            }
            
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateOrderStatus(id, status);
            
            logger.info("Order {} status updated to {} by admin: {}", id, status, auth.getName());
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid order status: {}", request.get("status"));
            return ResponseEntity.badRequest().body("Invalid order status");
        } catch (Exception e) {
            logger.error("Failed to update order status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update order status: " + e.getMessage());
        }
    }
}