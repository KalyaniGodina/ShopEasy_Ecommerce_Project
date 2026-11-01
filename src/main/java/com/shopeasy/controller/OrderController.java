package com.shopeasy.controller;

import com.shopeasy.dto.OrderRequest;
import com.shopeasy.model.CustomerOrder;
import com.shopeasy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /** Place an order as the signed-in user. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder place(@RequestBody OrderRequest req, Authentication auth) {
        return service.place(req, auth.getName());
    }

    /** The signed-in user's own orders. */
    @GetMapping("/my")
    public List<CustomerOrder> mine(Authentication auth) { return service.mine(auth.getName()); }

    /** All orders (admin only - enforced in SecurityConfig). */
    @GetMapping
    public List<CustomerOrder> all() { return service.all(); }

    @GetMapping("/{id}")
    public CustomerOrder get(@PathVariable Long id, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.get(id, auth.getName(), admin);
    }
}
