package com.dailycodework.dream_shops.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
public class DeliveryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Getters and setters
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    private int quantity;

    public DeliveryItem() {}

    public DeliveryItem(Delivery delivery, Product product, int quantity) {
        this.delivery = delivery;
        this.product = product;
        this.quantity = quantity;
    }

}
