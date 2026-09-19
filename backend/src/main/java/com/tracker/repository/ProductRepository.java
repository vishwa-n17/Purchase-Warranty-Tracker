package com.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.purchasewarrantytracker.model.Product;

@Repository("trackerProductRepository")
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByIdAndUserId(Long productId, Long userId);
    java.util.Optional<Product> findByIdAndUserId(Long id, Long userId);
}
