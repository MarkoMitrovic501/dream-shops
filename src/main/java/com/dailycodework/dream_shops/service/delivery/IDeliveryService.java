package com.dailycodework.dream_shops.service.delivery;

import com.dailycodework.dream_shops.dto.DeliveryDto;
import com.dailycodework.dream_shops.model.Delivery;
import com.dailycodework.dream_shops.request.DeliveryUpdateRequest;
import jakarta.transaction.Transactional;

import javax.naming.InsufficientResourcesException;

public interface IDeliveryService {

    Delivery createDelivery(Long userId, Long magacinId, Long productId, int quantity);

    Delivery placeDelivery(Long userId);

    void addProductToDelivery(Long deliveryId, Long productId, int quantity) throws InsufficientResourcesException;

    Delivery getDelivery(Long deliveryId);

    DeliveryDto convertToDto(Delivery delivery);

    void deleteDelivery(Long deliveryId);

    Delivery updateDelivery(DeliveryUpdateRequest request, Long deliveryId);

    @Transactional
    Delivery overwriteUpdate(DeliveryUpdateRequest request, Long deliveryId);

//    @Transactional
//    Delivery overwriteUpdate(DeliveryUpdateRequest request, Long deliveryId);
}
