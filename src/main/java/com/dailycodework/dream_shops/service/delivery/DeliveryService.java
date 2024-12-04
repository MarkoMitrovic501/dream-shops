package com.dailycodework.dream_shops.service.delivery;

import com.dailycodework.dream_shops.dto.DeliveryDto;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Delivery;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.repository.DeliveryRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import com.dailycodework.dream_shops.request.DeliveryUpdateRequest;
import com.dailycodework.dream_shops.service.magacin.MagacinService;
import com.dailycodework.dream_shops.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService {


    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final UserService userService;
    private final MagacinService magacinService;
    private final ProductRepository productRepository;


    public Delivery placeDelivery(Long userId) {

        User user = userService.findUserById(userId);
        if (user == null) {
            System.out.println("User not found for userId: " + userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        logger.info("User found: {}", user.getId());

        Delivery delivery = new Delivery();
        delivery.setUser(user);
        delivery.setDeliveryDate(LocalDate.now());

        Magacin magacin = magacinService.getMagacinByUserId(user.getId());
        if (magacin == null) {
            System.out.println("Magacin not found for userId: " + userId);
            throw new ResourceNotFoundException("No Magacin found for user with id: " + userId);
        }

        logger.info("Magacin found: {}", magacin.getName());

        delivery.setMagacin(magacin);

        return deliveryRepository.save(delivery);
    }

    private void adjustInventory(Product product, int quantityDifference) {
        if (quantityDifference == 0) {
            logger.debug("No inventory adjustment needed for product: {}", product.getName());
            return;
        }

        int currentStock = product.getInventory();
        int updatedStock = currentStock + quantityDifference;

        if (updatedStock < 0) {
            logger.error("Insufficient stock for product: {}. Current: {}, Required: {}",
                    product.getName(), currentStock, Math.abs(quantityDifference));
            throw new EmptyStackException();
        }

        product.setInventory(updatedStock);
        productRepository.save(product);

        logger.info("Inventory updated for product: {}. New stock: {}", product.getName(), updatedStock);
    }

    @Override
    public void addProductToDelivery(Long deliveryId, Long productId, int quantity) {
        logger.info("Adding productId: {} to deliveryId: {} with quantity: {}", productId, deliveryId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + productId));

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));

        Integer currentQuantity = delivery.getQuantity(product);
        currentQuantity = (currentQuantity == null) ? 0 : currentQuantity;

        int quantityDifference = quantity - currentQuantity;

        try {
            adjustInventory(product, -quantityDifference); // Adjust stock based on quantity difference
        } catch (IllegalArgumentException e) {
            logger.error("Failed to adjust inventory: {}", e.getMessage());
            throw e; // or handle it appropriately
        }

        delivery.addProduct(product, quantity);
        delivery.updateTotalPrice();
        deliveryRepository.save(delivery);

        logger.info("Product added to delivery. Updated quantity: {}, Total price: {}", quantity, delivery.getTotalPrice());
    }

    @Override
    public DeliveryDto convertToDto(Delivery delivery) {
        if (delivery == null) {
            throw new ResourceNotFoundException("Delivery not found.");
        }
        DeliveryDto dto = new DeliveryDto();
        dto.setId(delivery.getId());
        dto.setUserId(delivery.getUser().getId());
        dto.setMagacinId(delivery.getMagacin().getId());
        dto.setDeliveryDate(delivery.getDeliveryDate());
        dto.setTotalPrice(delivery.getTotalPrice());
        return dto;
    }

    @Override
    public Delivery getDelivery(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));
    }

    @Override
    public void deleteDelivery(Long deliveryId) {
        logger.warn("Deleting delivery with id: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));

        delivery.getProductQuantities().forEach(this::adjustInventory);
        deliveryRepository.delete(delivery);
    }

    @Transactional
    public Delivery updateDelivery(DeliveryUpdateRequest request, Long deliveryId) {
        logger.info("Updating delivery with id: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    logger.error("Delivery not found for id: {}", deliveryId);
                    return new ResourceNotFoundException("Delivery not found for id: " + deliveryId);
                });

        Map<Long, Product> products = productRepository.findAllById(request.getProducts().keySet())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        logger.debug("Products fetched for update: {}", products.keySet());

        decreaseInventoryForNewQuantities(request.getProducts(), products);

        Map<Product, Integer> updatedProductQuantities = request.getProducts().entrySet().stream()
                .collect(Collectors.toMap(entry -> products.get(entry.getKey()), Map.Entry::getValue));

        delivery.setProductQuantities(updatedProductQuantities);
        delivery.updateTotalPrice();

        Delivery savedDelivery = deliveryRepository.save(delivery);
        logger.info("Updated delivery saved with id: {}, Total Price: {}", savedDelivery.getId(), savedDelivery.getTotalPrice());

        return savedDelivery;
    }

    private void decreaseInventoryForNewQuantities(Map<Long, Integer> newQuantities, Map<Long, Product> products) {
        newQuantities.forEach((productId, newQuantity) -> {
            Product product = products.get(productId);

            logger.debug("Decreasing inventory for product: {}, Current stock: {}, Decrease by: {}",
                    product.getName(), product.getInventory(), newQuantity);

            adjustInventory(product, -newQuantity);

            logger.info("Inventory decreased for product: {}. New stock: {}", product.getName(), product.getInventory());
        });
    }

    @Transactional
    @Override
    public Delivery overwriteUpdate(DeliveryUpdateRequest request, Long deliveryId) {
        logger.info("Starting overwrite update for deliveryId: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    logger.error("Delivery not found for id: {}", deliveryId);
                    return new ResourceNotFoundException("Delivery not found for id: " + deliveryId);
                });
        Delivery updatedDelivery = overwriteDelivery(delivery, request);
        Delivery savedDelivery = deliveryRepository.save(updatedDelivery);

        logger.info("Saved delivery to the database: {}", savedDelivery);

        return savedDelivery;
    }

    private Delivery overwriteDelivery(Delivery existingDelivery, DeliveryUpdateRequest request) {
        logger.debug("Overwriting delivery with id: {}", existingDelivery.getId());

        if (existingDelivery.getProductQuantities() != null) {
            for (Map.Entry<Product, Integer> entry : existingDelivery.getProductQuantities().entrySet()) {
                Product product = entry.getKey();
                int quantityInDelivery = entry.getValue();
                adjustInventory(product, quantityInDelivery);
                logger.info("Restored inventory for product: {} by quantity: {}", product.getName(), quantityInDelivery);
            }
        }
        existingDelivery.setProductQuantities(new HashMap<>());

        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            logger.info("Updating product quantities for delivery id: {}", existingDelivery.getId());

            Map<Product, Integer> newProductQuantities = request.getProducts().entrySet().stream()
                    .map(entry -> {
                        Product product = productRepository.findById(entry.getKey())
                                .orElseThrow(() -> {
                                    logger.error("Product not found with id: {}", entry.getKey());
                                    return new ResourceNotFoundException("Product not found!");
                                });
                        return Map.entry(product, entry.getValue());
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            existingDelivery.setProductQuantities(newProductQuantities);

            for (Map.Entry<Product, Integer> entry : newProductQuantities.entrySet()) {
                Product product = entry.getKey();
                int newQuantity = entry.getValue();
                adjustInventory(product, -newQuantity);
                logger.info("Adjusted inventory for product: {} by quantity: {}", product.getName(), -newQuantity);
            }

            existingDelivery.updateTotalPrice();
            logger.info("Updated total price for delivery id: {} to: {}", existingDelivery.getId(), existingDelivery.getTotalPrice());
        } else {
            logger.warn("No products found in request. Setting total price to 0 for delivery id: {}", existingDelivery.getId());
            existingDelivery.setTotalPrice(BigDecimal.ZERO);
        }

        return existingDelivery;
    }
}