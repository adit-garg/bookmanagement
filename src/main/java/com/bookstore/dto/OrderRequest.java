// OrderRequest.java
package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class OrderRequest {
    @NotEmpty
    private List<OrderItemRequest> items;
    
    @NotBlank
    private String shippingAddress;
    
    public OrderRequest() {}
    
    public OrderRequest(List<OrderItemRequest> items, String shippingAddress) {
        this.items = items;
        this.shippingAddress = shippingAddress;
    }
    
    public List<OrderItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public static class OrderItemRequest {
        @NotNull
        private Long bookId;
        
        @NotNull
        @Positive
        private Integer quantity;
        
        public OrderItemRequest() {}
        
        public OrderItemRequest(Long bookId, Integer quantity) {
            this.bookId = bookId;
            this.quantity = quantity;
        }
        
        public Long getBookId() {
            return bookId;
        }
        
        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}