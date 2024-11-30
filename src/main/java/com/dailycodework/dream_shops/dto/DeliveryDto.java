package com.dailycodework.dream_shops.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class DeliveryDto {
    private Long id;
    private Long userId;
    private Long magacinId;
    private BigDecimal totalPrice;
    private Set<Long> productId;

    public void setDeliveryDate(LocalDate deliveryDate) {
    }
}
