package com.dailycodework.dream_shops.service.magacin;

import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.request.AddMagacinRequest;

import java.util.Set;

public interface IMagacinService {

    Magacin getMagacin(Long id);

    void clearMagacin(Long id);

    Magacin createMagacin(AddMagacinRequest request);

    void addProductToMagacin(Long magacinId, Long productId, int quantity);

    void deleteMagacinById(Long magacinId);

    Set<Product> countUniqueProductsInMagacin(Long magacinId);
}
