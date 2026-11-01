package com.shopeasy.service;

import com.shopeasy.dto.OrderRequest;
import com.shopeasy.model.CustomerOrder;
import com.shopeasy.model.OrderItem;
import com.shopeasy.model.Product;
import com.shopeasy.repository.OrderRepository;
import com.shopeasy.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Transactional
    public CustomerOrder place(OrderRequest req, String userEmail) {
        if (req == null || blank(req.customerName()) || blank(req.email()) || blank(req.address()))
            throw bad("Name, email and address are required");
        if (req.items() == null || req.items().isEmpty())
            throw bad("Cart is empty");

        CustomerOrder order = new CustomerOrder();
        order.setUserEmail(userEmail);
        order.setCustomerName(req.customerName().trim());
        order.setEmail(req.email().trim());
        order.setPhone(req.phone());
        order.setAddress(req.address().trim());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.Item it : req.items()) {
            if (it.quantity() <= 0) throw bad("Quantity must be at least 1");
            Product p = products.findById(it.productId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + it.productId()));
            if (p.getSizes() != null && !p.getSizes().isBlank()) {
                String chosen = it.size() == null ? "" : it.size().trim();
                boolean ok = Arrays.stream(p.getSizes().split(","))
                        .anyMatch(s -> s.trim().equalsIgnoreCase(chosen));
                if (!ok) throw bad("Please choose a valid size for " + p.getName());
            }
            if (p.getStock() < it.quantity())
                throw bad("Not enough stock for " + p.getName() + " (available: " + p.getStock() + ")");

            p.setStock(p.getStock() - it.quantity());
            products.save(p);

            // price always comes from the database, never from the client
            order.getItems().add(new OrderItem(p.getId(), p.getName(), it.size(), it.quantity(), p.getPrice()));
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(it.quantity())));
        }
        order.setTotal(total);
        return orders.save(order);
    }

    public List<CustomerOrder> all() { return orders.findAllByOrderByCreatedAtDesc(); }

    public List<CustomerOrder> mine(String userEmail) {
        return orders.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    /** Customers can only open their own orders; admins can open any. */
    public CustomerOrder get(Long id, String userEmail, boolean admin) {
        CustomerOrder o = orders.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        if (!admin && !userEmail.equals(o.getUserEmail()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        return o;
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
