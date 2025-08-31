// DataInitializer.java
// Create this file in: src/main/java/com/bookstore/config/DataInitializer.java

package com.bookstore.config;

import com.bookstore.entity.User;
import com.bookstore.entity.UserRole; // Adjust import based on your enum location
import com.bookstore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void initData() {
        logger.info("Initializing application data...");
        
        // Create admin user if it doesn't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Creating admin user...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@bookstore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN); // Adjust this based on your UserRole enum
            admin.setCustomerId("ADMIN001");
            admin.setAddress("Admin Address");
            admin.setAge(30);
            admin.setCreatedDate(LocalDateTime.now()); // Using LocalDateTime instead of Date
            
            try {
                userRepository.save(admin);
                logger.info("Admin user created successfully with username: admin and password: admin123");
            } catch (Exception e) {
                logger.error("Failed to create admin user: {}", e.getMessage());
            }
        } else {
            logger.info("Admin user already exists, skipping creation");
        }
    }
}