// AuthController.java - IMPROVED VERSION
package com.bookstore.controller;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.dto.AuthResponse;
import com.bookstore.entity.User;
import com.bookstore.service.UserService;
import com.bookstore.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for username: {}", request.getUsername());
            
            User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getAddress(),
                request.getAge()
            );
            
            User savedUser = userService.registerUser(user);
            
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return ResponseEntity.ok(new AuthResponse(
                "User registered successfully",
                null,
                savedUser.getUsername(),
                savedUser.getRole().toString()
            ));
        } catch (Exception e) {
            logger.error("Registration failed for username {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for username: {}", request.getUsername());
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            logger.debug("Authentication successful for: {}", request.getUsername());
            
            // Find user details
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            
            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(new AuthResponse(
                "Login successful",
                token,
                user.getUsername(),
                user.getRole().toString()
            ));
        } catch (BadCredentialsException e) {
            logger.warn("Bad credentials for username: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Invalid username or password");
        } catch (Exception e) {
            logger.error("Login failed for username {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.debug("Token validation request received");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format");
                return ResponseEntity.badRequest().body("Invalid authorization header");
            }
            
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                logger.debug("Token validation successful for user: {}", username);
                return ResponseEntity.ok(new AuthResponse(
                    "Token is valid",
                    token,
                    user.getUsername(),
                    user.getRole().toString()
                ));
            } else {
                logger.warn("Token validation failed");
                return ResponseEntity.status(401).body("Token is invalid or expired");
            }
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.status(401).body("Token validation failed: " + e.getMessage());
        }
    }
}