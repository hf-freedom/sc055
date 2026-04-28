package com.distribution.backend.controller;

import com.distribution.backend.model.Order;
import com.distribution.backend.model.Product;
import com.distribution.backend.model.User;
import com.distribution.backend.repository.ProductRepository;
import com.distribution.backend.repository.UserRepository;
import com.distribution.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3004")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.getOrderById(id);
        return orderOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{orderNo}")
    public ResponseEntity<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        Optional<Order> orderOpt = orderService.getOrderByOrderNo(orderNo);
        return orderOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        logger.info("Received createOrder request: {}", request);
        
        Long userId = parseLong(request.get("userId"));
        Long productId = parseLong(request.get("productId"));
        Integer quantity = parseInteger(request.get("quantity"));

        logger.info("Parsed values - userId: {}, productId: {}, quantity: {}", userId, productId, quantity);

        if (userId == null) {
            logger.error("Invalid userId: {}", request.get("userId"));
            List<User> allUsers = userRepository.findAll();
            logger.info("Available users in DB: {}", allUsers.size());
            for (User u : allUsers) {
                logger.info("  - User: id={}, username={}", u.getId(), u.getUsername());
            }
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid user ID: " + request.get("userId"));
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User u : allUsers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", u.getId());
                userMap.put("username", u.getUsername());
                userList.add(userMap);
            }
            error.put("debug", new HashMap<String, Object>() {{
                put("availableUsers", userList);
            }});
            return ResponseEntity.badRequest().body(error);
        }

        if (productId == null) {
            logger.error("Invalid productId: {}", request.get("productId"));
            List<Product> allProducts = productRepository.findAll();
            logger.info("Available products in DB: {}", allProducts.size());
            for (Product p : allProducts) {
                logger.info("  - Product: id={}, name={}, stock={}", p.getId(), p.getName(), p.getStock());
            }
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid product ID: " + request.get("productId"));
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product p : allProducts) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", p.getId());
                productMap.put("name", p.getName());
                productMap.put("stock", p.getStock());
                productList.add(productMap);
            }
            error.put("debug", new HashMap<String, Object>() {{
                put("availableProducts", productList);
            }});
            return ResponseEntity.badRequest().body(error);
        }

        if (quantity == null || quantity <= 0) {
            logger.error("Invalid quantity: {}", quantity);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid quantity: " + quantity);
            return ResponseEntity.badRequest().body(error);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.error("User not found: {}", userId);
            List<User> allUsers = userRepository.findAll();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User not found: " + userId);
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User u : allUsers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", u.getId());
                userMap.put("username", u.getUsername());
                userList.add(userMap);
            }
            error.put("debug", new HashMap<String, Object>() {{
                put("availableUsers", userList);
            }});
            return ResponseEntity.badRequest().body(error);
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            logger.error("Product not found: {}", productId);
            List<Product> allProducts = productRepository.findAll();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Product not found: " + productId);
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product p : allProducts) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", p.getId());
                productMap.put("name", p.getName());
                productMap.put("stock", p.getStock());
                productList.add(productMap);
            }
            error.put("debug", new HashMap<String, Object>() {{
                put("availableProducts", productList);
            }});
            return ResponseEntity.badRequest().body(error);
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            logger.error("Insufficient stock. Product: {}, stock: {}, requested: {}", 
                product.getName(), product.getStock(), quantity);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("Creating order for user: {}, product: {}, quantity: {}", 
            userOpt.get().getUsername(), product.getName(), quantity);
        
        Optional<Order> result = orderService.createOrder(userId, productId, quantity);
        if (result.isPresent()) {
            logger.info("Order created successfully: {}", result.get().getId());
            return ResponseEntity.ok(result.get());
        } else {
            logger.error("Failed to create order for unknown reason");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        Optional<Order> result = orderService.payOrder(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order not found or cannot be paid");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        Optional<Order> result = orderService.shipOrder(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order not found or cannot be shipped");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        Optional<Order> result = orderService.completeOrder(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order not found or cannot be completed");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Optional<Order> result = orderService.cancelOrder(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order not found or cannot be cancelled");
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String strVal = value.toString().trim();
            if (strVal.isEmpty()) {
                return null;
            }
            return Long.parseLong(strVal);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            String strVal = value.toString().trim();
            if (strVal.isEmpty()) {
                return null;
            }
            return Integer.parseInt(strVal);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
