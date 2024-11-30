package com.dailycodework.dream_shops.service.magacin;

import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.request.AddMagacinRequest;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

public interface IMagacinService {

    Magacin getMagacin(Long id);

    Magacin clearMagacin(Long id);

    Magacin createMagacin(AddMagacinRequest request);

    @Transactional
    Magacin addProductToMagacin(Long magacinId, Long productId);

    Magacin initializeNewMagacin(User user);


    Magacin deleteMagacinById(Long magacinId);

    Magacin getProductById(Long productId);

    Optional<Magacin> getMagacinByIdAndUser(Long magacinId, User user);

    Set<Product> countUniqueProductsInMagacin(Long magacinId);

    Magacin findMagacinById(Long magacinId);
}
