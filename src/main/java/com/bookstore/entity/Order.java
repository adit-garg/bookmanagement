// Order.java - FIXED VERSION WITH JSON ANNOTATIONS
package com.bookstore.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "order_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // This prevents circular reference
    private List<OrderItem> orderItems;
    
    private String address;
    
    // Constructors
    public Order() {}
    
    public Order(User user, BigDecimal totalAmount, String address) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.address = address;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { 
        this.orderItems = orderItems;
        // Set the bidirectional relationship
        if (orderItems != null) {
            orderItems.forEach(item -> item.setOrder(this));
        }
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}