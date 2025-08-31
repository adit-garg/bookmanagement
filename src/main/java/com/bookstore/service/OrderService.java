// OrderService.java
package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.dto.OrderRequest;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Transactional
    public Order createOrder(User user, List<OrderRequest.OrderItemRequest> items, String address) {
        try {
            if (items == null || items.isEmpty()) {
                throw new RuntimeException("Order items cannot be empty");
            }
            
            Order order = new Order();
            order.setUser(user);
            order.setAddress(address);
            order.setOrderDate(LocalDateTime.now());
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            
            for (OrderRequest.OrderItemRequest itemRequest : items) {
                if (itemRequest.getBookId() == null || itemRequest.getQuantity() == null) {
                    throw new RuntimeException("Book ID and quantity are required for each item");
                }
                
                Long bookId = itemRequest.getBookId();
                Integer quantity = itemRequest.getQuantity();
                
                if (quantity <= 0) {
                    throw new RuntimeException("Quantity must be greater than 0");
                }
                
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
                
                if (book.getStock() < quantity) {
                    throw new RuntimeException("Insufficient stock for book: " + book.getTitle() + ". Available: " + book.getStock() + ", Requested: " + quantity);
                }
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setBook(book);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(book.getPrice());
                
                // Add to the list
                orderItems.add(orderItem);
                
                totalAmount = totalAmount.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));
                
                // Update stock
                book.setStock(book.getStock() - quantity);
                bookRepository.save(book);
            }
            
            // Set the order items and total amount
            order.setOrderItems(orderItems);
            order.setTotalAmount(totalAmount);
            
            // Save the order (this will cascade to save OrderItems)
            Order savedOrder = orderRepository.save(order);
            
            return savedOrder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}