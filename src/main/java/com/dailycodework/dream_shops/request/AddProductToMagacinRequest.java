package com.dailycodework.dream_shops.request;

import lombok.Data;

@Data
public class AddProductToMagacinRequest {
    private Long magacinId;
    private Long productId;
}
