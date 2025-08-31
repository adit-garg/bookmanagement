// JwtAuthenticationFilter.java - COMPLETELY FIXED VERSION
package com.bookstore.security;

import com.bookstore.service.UserService;
import com.bookstore.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Checking if should filter path: {} method: {}", path, method);
        
        // Skip JWT filtering for public endpoints and static resources
        boolean shouldSkip = path.startsWith("/api/auth/") || 
               //path.startsWith("/api/books/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/error") ||
               path.equals("/favicon.ico") ||
               method.equals("OPTIONS"); // Skip preflight requests
               
        logger.debug("Should skip filtering: {}", shouldSkip);
        return shouldSkip;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.info("Processing authenticated request: {} {}", method, requestURI);
        
        String header = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        
        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
            logger.debug("Found Bearer token in request");
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (Exception e) {
                logger.error("JWT Token parsing failed: {}", e.getMessage());
                // Clear any existing authentication
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.warn("No Authorization header or Bearer token found for protected endpoint: {}", requestURI);
        }
        
        // Only proceed if we have a username and no existing authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwt)) {
                    logger.debug("JWT token is valid, setting up authentication for: {}", username);
                    setUpAuthentication(username, request);
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                logger.error("Error during JWT validation: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else if (username == null) {
            logger.warn("No username extracted from token for protected endpoint: {}", requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void setUpAuthentication(String username, HttpServletRequest request) {
        try {
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                logger.error("User not found in database for username: {}", username);
                SecurityContextHolder.clearContext();
                return;
            }
            
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
            
            // CRITICAL FIX: Use username string as principal, not User object
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.info("Authentication successfully set for user: {} with role: {}", username, user.getRole());
            
        } catch (Exception e) {
            logger.error("Error setting up authentication for user {}: {}", username, e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}