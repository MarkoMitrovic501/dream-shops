package com.dailycodework.dream_shops.model;

import com.dailycodework.dream_shops.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalPrice = BigDecimal.ZERO;

    private static final BigDecimal DELIVERY_COST = new BigDecimal("99.99");

    private LocalDate deliveryDate;

    @ManyToOne
    @JoinColumn(name = "magacin_id")
    @JsonIgnore
    private Magacin magacin;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.REMOVE)
    private List<Product> products;

    @ElementCollection
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> productQuantities = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryItem> deliveryItems = new ArrayList<>();

    public List<Product> getProducts() {
        return new ArrayList<>(productQuantities.keySet());
    }

    public Integer getQuantity(Product product) {
        if (product == null || productQuantities == null) {
            return 0;
        }
        return productQuantities.getOrDefault(product, 0);  // Return 0 if the product is not found
    }

    public int getCount() {
        return productQuantities.size();
    }

//    public void addProduct(Product product, int quantity) {
//        this.productQuantities.put(product, quantity);
//        updateTotalPrice();
//
//    }

//    public void addProduct(Product product, int quantity) {
//        DeliveryItem deliveryItem = new DeliveryItem(this, product, quantity);
//        this.deliveryItems.add(deliveryItem);
//    }


//    public void addProduct(Product product, int quantity) {
//        if (productQuantities.containsKey(product)) {
//            int currentQuantity = productQuantities.get(product);
//            productQuantities.put(product, currentQuantity + quantity);
//        } else {
//            productQuantities.put(product, quantity);
//        }
//        updateTotalPrice();
//    }

    public void addProduct(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }

        // If product already exists in the delivery, update quantity
        productQuantities.put(product, productQuantities.getOrDefault(product, 0) + quantity);

        // Optionally update the total price
        updateTotalPrice();
    }
    
    public void removeProduct(Product product) {
        this.productQuantities.remove(product);
        updateTotalPrice();
    }

    public void updateTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            BigDecimal productPrice = entry.getKey().getPrice();
            totalPrice = totalPrice.add(productPrice.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        this.totalPrice = totalPrice;
    }
}