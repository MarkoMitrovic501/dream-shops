package com.dailycodework.dream_shops.dto;

import com.dailycodework.dream_shops.model.Product;
import lombok.Data;

import java.util.Set;

@Data
public class MagacinDto {
    private Long magacinId;
    private Set<Product> items;
}
