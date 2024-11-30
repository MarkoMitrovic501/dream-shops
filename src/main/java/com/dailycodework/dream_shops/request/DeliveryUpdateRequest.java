package com.dailycodework.dream_shops.request;

import com.dailycodework.dream_shops.enums.DeliveryStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Map;

@Setter
@Getter
@Data
public class DeliveryUpdateRequest {
    private Long userId;
    private Long magacinId;
    private LocalDate deliveryDate;
    private Map<Long, Integer> products;
    private DeliveryStatus status;
}
