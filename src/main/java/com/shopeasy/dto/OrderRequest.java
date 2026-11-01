package com.shopeasy.dto;

import java.util.List;

public record OrderRequest(String customerName, String email, String phone, String address, List<Item> items) {
    public record Item(Long productId, String size, int quantity) {}
}
