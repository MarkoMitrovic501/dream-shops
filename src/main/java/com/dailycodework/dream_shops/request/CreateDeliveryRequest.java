package com.dailycodework.dream_shops.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Data
public class CreateDeliveryRequest {

    private Long userId;
    private Long productId;
    private int quantity;
    private Long products;
   // private LocalDate deliveryDate;
}