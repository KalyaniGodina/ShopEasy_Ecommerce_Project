package com.shopeasy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Comma separated list of available sizes / variants, e.g. "6,7,8,9,10" or "S,M,L,XL". */
    private String sizes;

    private String color;

    private int stock;

    /** Path or URL of the product image, e.g. /images/running-shoes.svg */
    private String imageUrl;

    private double rating;

    public Product() {}

    public Product(String name, String description, String brand, String category, BigDecimal price,
                   String sizes, String color, int stock, String imageUrl, double rating) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.sizes = sizes;
        this.color = color;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
