// Book.java
package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String author;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Lob
    private String description;
    
    @NotBlank
    private String category;
    
    @NotNull
    @Positive
    private Integer stock;
    
    // Constructors, Getters, and Setters
    public Book() {}
    
    public Book(String title, String author, BigDecimal price, String description, String category, Integer stock) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stock = stock;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}