package com.distribution.backend.repository;

import com.distribution.backend.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {
    private static final Map<Long, Product> products = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(4);
    private static volatile boolean initialized = false;

    public ProductRepository() {
        synchronized (ProductRepository.class) {
            if (!initialized) {
                initData();
                initialized = true;
            }
        }
    }

    private void initData() {
        if (!products.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        saveInternal(Product.builder()
                .id(1L)
                .name("测试商品A")
                .description("这是一款测试商品，支持二级分销")
                .price(new BigDecimal("100.00"))
                .stock(100)
                .isDistributionEnabled(true)
                .firstLevelCommissionRate(new BigDecimal("0.10"))
                .secondLevelCommissionRate(new BigDecimal("0.05"))
                .createdAt(now)
                .updatedAt(now)
                .build());

        saveInternal(Product.builder()
                .id(2L)
                .name("测试商品B")
                .description("这是一款不参与分销的商品")
                .price(new BigDecimal("50.00"))
                .stock(200)
                .isDistributionEnabled(false)
                .firstLevelCommissionRate(BigDecimal.ZERO)
                .secondLevelCommissionRate(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build());

        saveInternal(Product.builder()
                .id(3L)
                .name("测试商品C")
                .description("高佣金商品，一级15%，二级8%")
                .price(new BigDecimal("299.00"))
                .stock(50)
                .isDistributionEnabled(true)
                .firstLevelCommissionRate(new BigDecimal("0.15"))
                .secondLevelCommissionRate(new BigDecimal("0.08"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private Product saveInternal(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        products.put(product.getId(), product);
        return product;
    }

    public Product save(Product product) {
        return saveInternal(product);
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
}
