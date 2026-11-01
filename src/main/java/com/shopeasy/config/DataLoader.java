package com.shopeasy.config;

import com.shopeasy.model.Product;
import com.shopeasy.model.User;
import com.shopeasy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.shopeasy.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Seeds the database with sample products on first start. Each product uses a matching image. */
@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository repo;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public DataLoader(ProductRepository repo, UserRepository users, PasswordEncoder encoder,
                      @Value("${app.admin.name}") String adminName,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword) {
        this.repo = repo;
        this.users = users;
        this.encoder = encoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(adminEmail.toLowerCase())) {
            users.save(new User(adminName, adminEmail.toLowerCase(), encoder.encode(adminPassword), "ADMIN"));
        }
        if (repo.count() > 0) return;

        repo.saveAll(List.of(
            p("Velocity Running Shoes", "Lightweight, breathable running shoes with a cushioned sole for daily runs.",
              "Nike", "Footwear", "4999", "6,7,8,9,10,11", "Red", 40, "running-shoes.jpg", 4.5),
            p("Classic High-Top Sneakers", "Everyday high-top sneakers with a durable rubber outsole.",
              "Adidas", "Footwear", "3799", "6,7,8,9,10", "White", 35, "sneakers.jpg", 4.3),
            p("Cotton Crew Neck T-Shirt", "Soft 100% cotton t-shirt with a relaxed everyday fit.",
              "Puma", "Clothing", "899", "S,M,L,XL,XXL", "Teal", 120, "tshirt.jpg", 4.1),
            p("Slim Fit Denim Jeans", "Stretch denim jeans with a modern slim fit and five-pocket styling.",
              "Levi's", "Clothing", "2499", "28,30,32,34,36", "Blue", 60, "jeans.jpg", 4.4),
            p("Fleece Pullover Hoodie", "Warm brushed-fleece hoodie with a drawstring hood and kangaroo pocket.",
              "H&M", "Clothing", "1899", "S,M,L,XL", "Purple", 50, "hoodie.jpg", 4.2),
            p("Chronograph Wrist Watch", "Gold-tone analog watch with a scratch-resistant glass dial.",
              "Fossil", "Accessories", "7999", "Free Size", "Gold", 25, "watch.jpg", 4.6),
            p("Trek 35L Backpack", "Water-resistant 35L backpack with padded straps and laptop compartment.",
              "Wildcraft", "Accessories", "1799", "35L", "Orange", 45, "backpack.jpg", 4.3),
            p("Aviator Sunglasses", "UV400 protected sunglasses with a lightweight frame.",
              "Ray-Ban", "Accessories", "5499", "Free Size", "Black", 30, "sunglasses.jpg", 4.5),
            p("Sports Baseball Cap", "Adjustable cotton cap with a curved brim and breathable stitching.",
              "Nike", "Accessories", "799", "Free Size", "Navy", 80, "cap.jpg", 4.0),
            p("Wireless Noise-Cancelling Headphones", "Over-ear Bluetooth headphones with active noise cancellation and 30h battery.",
              "Sony", "Electronics", "17990", "Standard", "Black", 20, "headphones.jpg", 4.7),
            p("Galaxy 5G Smartphone", "6.5-inch AMOLED display, 50MP camera and all-day battery.",
              "Samsung", "Electronics", "24999", "128GB,256GB", "Blue", 30, "smartphone.jpg", 4.4),
            p("Pro Slim Laptop 14-inch", "Thin and light 14-inch laptop with a fast SSD for work and study.",
              "HP", "Electronics", "54999", "8GB/512GB,16GB/1TB", "Silver", 15, "laptop.jpg", 4.5)
        ));
    }

    private static Product p(String name, String desc, String brand, String category, String price,
                             String sizes, String color, int stock, String image, double rating) {
        return new Product(name, desc, brand, category, new BigDecimal(price), sizes, color, stock,
                "/images/" + image, rating);
    }
}
