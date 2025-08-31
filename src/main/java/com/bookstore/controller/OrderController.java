// OrderController.java - UPDATED WITH IMPROVED ADMIN ENDPOINT
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest request, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Order creation request received");
            logger.debug("Authentication object: {}", auth);
            logger.debug("Request: {}", request);
            
            if (auth == null) {
                logger.error("No authentication found in security context");
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String username = auth.getName();
            if (username == null || username.isEmpty()) {
                logger.error("Username is null or empty from authentication");
                response.put("success", false);
                response.put("message", "Invalid authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
                logger.warn("Invalid order request - missing items");
                response.put("success", false);
                response.put("message", "Order request and items are required");
                return ResponseEntity.badRequest().body(response);
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
            
            // Create a simplified response to avoid serialization issues
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("orderId", order.getId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("status", order.getStatus().toString());
            response.put("orderDate", order.getOrderDate().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to create order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(Authentication auth) {
        try {
            if (auth == null) {
                logger.error("No authentication found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication required"));
            }
            
            String username = auth.getName();
            logger.debug("Fetching orders for user: {}", username);
            
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            List<Order> orders = orderService.getUserOrders(user);
            logger.info("Found {} orders for user: {}", orders.size(), username);
            
            // Convert orders to DTOs to avoid circular reference issues
            List<Map<String, Object>> orderDtos = orders.stream().map(order -> {
                Map<String, Object> orderDto = new HashMap<>();
                orderDto.put("id", order.getId());
                orderDto.put("orderDate", order.getOrderDate());
                orderDto.put("totalAmount", order.getTotalAmount());
                orderDto.put("status", order.getStatus().toString());
                orderDto.put("address", order.getAddress());
                
                // Convert order items
                List<Map<String, Object>> itemDtos = order.getOrderItems().stream().map(item -> {
                    Map<String, Object> itemDto = new HashMap<>();
                    itemDto.put("id", item.getId());
                    itemDto.put("quantity", item.getQuantity());
                    itemDto.put("price", item.getPrice());
                    
                    // Book details
                    Map<String, Object> bookDto = new HashMap<>();
                    bookDto.put("id", item.getBook().getId());
                    bookDto.put("title", item.getBook().getTitle());
                    bookDto.put("author", item.getBook().getAuthor());
                    bookDto.put("category", item.getBook().getCategory());
                    bookDto.put("price", item.getBook().getPrice());
                    itemDto.put("book", bookDto);
                    
                    return itemDto;
                }).collect(Collectors.toList());
                
                orderDto.put("orderItems", itemDtos);
                return orderDto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "orders", orderDtos,
                "count", orderDtos.size(),
                "message", "User orders retrieved successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch user orders: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve orders: " + e.getMessage());
            errorResponse.put("orders", List.of());
            
            return ResponseEntity.ok(errorResponse); // Return 200 with error info for consistency
        }
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllOrdersForAdmin(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Admin order retrieval requested by: {}", auth != null ? auth.getName() : "unknown");
            
            List<Order> orders = orderService.getAllOrders();
            
            // Convert orders to DTOs similar to user orders for consistency
            List<Map<String, Object>> orderDtos = orders.stream().map(order -> {
                Map<String, Object> orderDto = new HashMap<>();
                orderDto.put("id", order.getId());
                orderDto.put("orderDate", order.getOrderDate());
                orderDto.put("totalAmount", order.getTotalAmount());
                orderDto.put("status", order.getStatus().toString());
                orderDto.put("address", order.getAddress());
                
                // Add user information for admin view
                if (order.getUser() != null) {
                    Map<String, Object> userDto = new HashMap<>();
                    userDto.put("id", order.getUser().getId());
                    userDto.put("username", order.getUser().getUsername());
                    userDto.put("email", order.getUser().getEmail());
                    orderDto.put("user", userDto);
                }
                
                // Convert order items
                List<Map<String, Object>> itemDtos = order.getOrderItems().stream().map(item -> {
                    Map<String, Object> itemDto = new HashMap<>();
                    itemDto.put("id", item.getId());
                    itemDto.put("quantity", item.getQuantity());
                    itemDto.put("price", item.getPrice());
                    
                    // Book details
                    Map<String, Object> bookDto = new HashMap<>();
                    bookDto.put("id", item.getBook().getId());
                    bookDto.put("title", item.getBook().getTitle());
                    bookDto.put("author", item.getBook().getAuthor());
                    bookDto.put("category", item.getBook().getCategory());
                    bookDto.put("price", item.getBook().getPrice());
                    itemDto.put("book", bookDto);
                    
                    return itemDto;
                }).collect(Collectors.toList());
                
                orderDto.put("orderItems", itemDtos);
                return orderDto;
            }).collect(Collectors.toList());
            
            response.put("success", true);
            response.put("orders", orderDtos);
            response.put("count", orderDtos.size());
            response.put("message", "Orders retrieved successfully");
            
            logger.info("Retrieved {} orders for admin view", orderDtos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to fetch all orders for admin: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve orders: " + e.getMessage());
            response.put("orders", List.of());
            
            return ResponseEntity.ok(response); // Return 200 with error info for consistency
        }
    }
    
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Order status update requested for order {} by admin: {}", orderId, auth != null ? auth.getName() : "unknown");
            
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                response.put("success", false);
                response.put("message", "Status is required");
                return ResponseEntity.ok(response); // Return 200 for consistency
            }
            
            // Validate status enum
            OrderStatus status;
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid order status provided: {}", statusStr);
                response.put("success", false);
                response.put("message", "Invalid order status. Valid values are: " + 
                    String.join(", ", java.util.Arrays.stream(OrderStatus.values())
                        .map(Enum::name).toArray(String[]::new)));
                return ResponseEntity.ok(response);
            }
            
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            
            logger.info("Order {} status updated to {} by admin: {}", orderId, status, auth.getName());
            
            // Create order DTO for response
            Map<String, Object> orderDto = new HashMap<>();
            orderDto.put("id", updatedOrder.getId());
            orderDto.put("status", updatedOrder.getStatus().toString());
            orderDto.put("totalAmount", updatedOrder.getTotalAmount());
            orderDto.put("orderDate", updatedOrder.getOrderDate());
            
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("order", orderDto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to update order status for order {}: {}", orderId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to update order status: " + e.getMessage());
            
            return ResponseEntity.ok(response); // Return 200 with error info for consistency
        }
    }
}