package com.shopeasy.controller;

import com.shopeasy.model.Product;
import com.shopeasy.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String brand,
                              @RequestParam(required = false) String sort) {
        return service.search(q, category, brand, sort);
    }

    @GetMapping("/categories")
    public List<String> categories() { return service.categories(); }

    @GetMapping("/brands")
    public List<String> brands() { return service.brands(); }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product p) { return service.create(p); }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) { return service.update(id, p); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
