package com.distribution.backend.service;

import com.distribution.backend.model.Product;
import com.distribution.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getIsDistributionEnabled() == null) {
            product.setIsDistributionEnabled(false);
        }
        if (product.getFirstLevelCommissionRate() == null) {
            product.setFirstLevelCommissionRate(BigDecimal.ZERO);
        }
        if (product.getSecondLevelCommissionRate() == null) {
            product.setSecondLevelCommissionRate(BigDecimal.ZERO);
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            return Optional.empty();
        }

        Product product = productOpt.get();
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStock() != null) {
            product.setStock(productDetails.getStock());
        }
        if (productDetails.getIsDistributionEnabled() != null) {
            product.setIsDistributionEnabled(productDetails.getIsDistributionEnabled());
        }
        if (productDetails.getFirstLevelCommissionRate() != null) {
            product.setFirstLevelCommissionRate(productDetails.getFirstLevelCommissionRate());
        }
        if (productDetails.getSecondLevelCommissionRate() != null) {
            product.setSecondLevelCommissionRate(productDetails.getSecondLevelCommissionRate());
        }

        return Optional.of(productRepository.save(product));
    }

    @Transactional
    public boolean decreaseStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return false;
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            return false;
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean increaseStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return false;
        }

        Product product = productOpt.get();
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        return true;
    }
}
