// User.java - FIXED VERSION
package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6)
    private String password;
    
    @NotBlank
    private String address;
    
    @Column(name = "customer_id", unique = true)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;
    
    private Integer age;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password, String address, Integer age) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.address = address;
        this.age = age;
        this.customerId = generateCustomerId();
    }
    
    private String generateCustomerId() {
        return "CUST" + System.currentTimeMillis();
    }
    
    // UserDetails implementation - THIS IS CRUCIAL FOR SPRING SECURITY
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert UserRole to Spring Security GrantedAuthority with ROLE_ prefix
        String roleName = "ROLE_" + role.name(); // This will be ROLE_CUSTOMER or ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // Regular getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public void setPassword(String password) { this.password = password; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}