package com.dailycodework.dream_shops.service;

import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.repository.MagacinRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefactorDependencies {

    private final ProductRepository productRepository;
    private final MagacinRepository magacinRepository;


    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public void addProductToMagacin(Long magacinId, Long productId) {
        Magacin magacin = magacinRepository.findById(magacinId)
                .orElseThrow(() -> new ResourceNotFoundException("Magacin not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!magacin.getItems().contains(product)) {
            product.setMagacin(magacin);
            magacin.addItem(product);
            productRepository.save(product);
        }
    }
}