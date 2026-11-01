package com.shopeasy.service;

import com.shopeasy.model.Product;
import com.shopeasy.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> search(String q, String category, String brand, String sort) {
        Comparator<Product> cmp = switch (sort == null ? "" : sort) {
            case "price_asc" -> Comparator.comparing(Product::getPrice);
            case "price_desc" -> Comparator.comparing(Product::getPrice).reversed();
            case "rating" -> Comparator.comparingDouble(Product::getRating).reversed();
            default -> Comparator.comparing(Product::getId);
        };
        String needle = q == null ? "" : q.trim().toLowerCase();
        return repo.findAll().stream()
                .filter(p -> isBlank(category) || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> isBlank(brand) || p.getBrand().equalsIgnoreCase(brand))
                .filter(p -> needle.isEmpty()
                        || p.getName().toLowerCase().contains(needle)
                        || p.getBrand().toLowerCase().contains(needle)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(needle)))
                .sorted(cmp)
                .toList();
    }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    public Product create(Product p) {
        validate(p);
        p.setId(null);
        return repo.save(p);
    }

    public Product update(Long id, Product in) {
        Product p = get(id);
        validate(in);
        p.setName(in.getName());
        p.setDescription(in.getDescription());
        p.setBrand(in.getBrand());
        p.setCategory(in.getCategory());
        p.setPrice(in.getPrice());
        p.setSizes(in.getSizes());
        p.setColor(in.getColor());
        p.setStock(in.getStock());
        p.setImageUrl(in.getImageUrl());
        p.setRating(in.getRating());
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.delete(get(id));
    }

    public List<String> categories() { return repo.findAllCategories(); }
    public List<String> brands() { return repo.findAllBrands(); }

    private void validate(Product p) {
        if (isBlank(p.getName()) || isBlank(p.getBrand()) || isBlank(p.getCategory()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, brand and category are required");
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be greater than 0");
        if (p.getStock() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock cannot be negative");
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
