package com.dailycodework.dream_shops.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMagacinRequest {
    private Long magacin_id;
    private String magacin_name;
    @NotNull(message = "User ID cannot be null")
    private Long userId;
}
