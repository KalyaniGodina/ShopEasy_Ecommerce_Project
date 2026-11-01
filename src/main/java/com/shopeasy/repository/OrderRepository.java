package com.shopeasy.repository;

import com.shopeasy.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    List<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
