package com.dailycodework.dream_shops.dto;

import com.dailycodework.dream_shops.model.Category;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductDto {
        private Long id;
        private String name;
        private String brand;
        private BigDecimal price;
        private int inventory;
        private String description;
        private Category category;
}
