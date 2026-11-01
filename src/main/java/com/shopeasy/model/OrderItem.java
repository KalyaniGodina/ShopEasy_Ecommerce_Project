package com.shopeasy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;
    private String size;
    private int quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(Long productId, String productName, String size, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
