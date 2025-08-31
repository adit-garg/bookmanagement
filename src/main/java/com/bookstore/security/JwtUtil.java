// JwtUtil.java
package com.bookstore.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(String username) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            
            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
            
            logger.debug("Generated JWT token for user: {}, expires at: {}", username, expiryDate);
            return token;
        } catch (Exception e) {
            logger.error("Error generating JWT token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
    
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String username = claims.getSubject();
            logger.debug("Extracted username from JWT token: {}", username);
            return username;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("JWT token security exception: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT token is empty or null: {}", e.getMessage());
            throw e;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                logger.warn("JWT token is expired");
                return false;
            }
            
            logger.debug("JWT token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.error("JWT token security exception: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT token is empty or null: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error validating JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Error extracting expiration date from JWT token: {}", e.getMessage());
            return null;
        }
    }
}