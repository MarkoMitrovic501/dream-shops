package com.dailycodework.dream_shops.service.delivery;

import com.dailycodework.dream_shops.dto.DeliveryDto;
import com.dailycodework.dream_shops.exceptions.InsufficientStockException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Delivery;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.repository.DeliveryRepository;
import com.dailycodework.dream_shops.repository.MagacinRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import com.dailycodework.dream_shops.repository.UserRepository;
import com.dailycodework.dream_shops.request.DeliveryUpdateRequest;
import com.dailycodework.dream_shops.service.magacin.MagacinService;
import com.dailycodework.dream_shops.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserService userService;
    private final MagacinService magacinService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MagacinRepository magacinRepository;

    @Transactional
    @Override
    public Delivery createDelivery(Long userId, Long magacinId, Long productId, int quantity) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found for id: " + userId);
        }

        Magacin magacin = magacinService.findMagacinById(magacinId);
        if (magacin == null) {
            throw new ResourceNotFoundException("Magacin not found for id: " + magacinId);
        }

        Delivery delivery = new Delivery();
        delivery.setUser(user);
        delivery.setMagacin(magacin);
        delivery.setDeliveryDate(LocalDate.now());
        delivery = deliveryRepository.save(delivery);

        if (productId != null && quantity > 0) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + productId));

            if (product.getInventory() < quantity) {
                throw new InsufficientStockException(
                        "Insufficient stock for productId: " + productId + ". Available: " + product.getInventory());
            }

            product.setInventory(product.getInventory() - quantity);
            productRepository.save(product);

            delivery.addProduct(product, quantity);
            deliveryRepository.save(delivery); // This should cascade and persist DeliveryItems
        }

        return delivery;
    }


    public Delivery placeDelivery(Long userId) {

        User user = userService.findUserById(userId);
        if (user == null) {
            System.out.println("User not found for userId: " + userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        System.out.println("User found: " + user.getId());

        Delivery delivery = new Delivery();
        delivery.setUser(user);
        delivery.setDeliveryDate(LocalDate.now());

        Magacin magacin = magacinService.getMagacinByUserId(user.getId());
        if (magacin == null) {
            System.out.println("Magacin not found for userId: " + userId);
            throw new ResourceNotFoundException("No Magacin found for user with id: " + userId);
        }

        System.out.println("Magacin found: " + magacin.getName());

        delivery.setMagacin(magacin);

        return deliveryRepository.save(delivery);
    }

    private void adjustInventory(Product product, int quantityChange) {
        if (quantityChange != 0) {
            int newInventory = product.getInventory() + quantityChange;
            if (newInventory < 0) {
                throw new InsufficientStockException("Not enough inventory for product id: " + product.getId());
            }
            product.setInventory(newInventory);
            System.out.println("quantityChange");
            productRepository.save(product);
        }
    }

    @Override
    public void addProductToDelivery(Long deliveryId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + productId));
        System.out.println("Product found for id: " + productId);

        if (product.getInventory() < quantity) {
            System.out.println("Insufficient stock for productId: " + productId + ". Available: " + product.getInventory());
            throw new InsufficientStockException(
                    "Insufficient stock for productId: " + productId + ". Available: " + product.getInventory());
        }
        System.out.println("DeliveryService 1");
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));
        System.out.println("DeliveryService 2");
        delivery.addProduct(product, quantity);
        deliveryRepository.save(delivery);
        System.out.println("DeliveryService 3");

        Integer currentQuantity = delivery.getQuantity(product);
        if (currentQuantity == null) {
            currentQuantity = 0;
        }
        int quantityDifference = quantity - currentQuantity;

        adjustInventory(product, -quantityDifference);

        delivery.addProduct(product, quantity);
        delivery.updateTotalPrice();

        deliveryRepository.save(delivery);
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
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));

        delivery.getProductQuantities().forEach(this::adjustInventory);
        deliveryRepository.delete(delivery);
    }

    @Transactional
    @Override
    public Delivery updateDelivery(DeliveryUpdateRequest request, Long deliveryId) {
        System.out.println("Starting delivery update for deliveryId: " + deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .map(existingDelivery -> {
                    System.out.println("Found existing delivery: " + existingDelivery);
                    Delivery updatedDelivery = updateExistingDelivery(existingDelivery, request);
                    System.out.println("Updated delivery details: " + updatedDelivery);
                    return updatedDelivery;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));

        System.out.println("Saving updated delivery: " + delivery);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        System.out.println("Saved delivery to the database: " + savedDelivery);

        return savedDelivery;
    }

    private Delivery updateExistingDelivery(Delivery existingDelivery, DeliveryUpdateRequest request) {
        // Apply updates from the request to the existing delivery
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
            existingDelivery.setUser(user);
        }

        if (request.getMagacinId() != null) {
            Magacin magacin = magacinRepository.findById(request.getMagacinId())
                    .orElseThrow(() -> new ResourceNotFoundException("Magacin not found!"));
            existingDelivery.setMagacin(magacin);
        }

        if (request.getDeliveryDate() != null) {
            existingDelivery.setDeliveryDate(request.getDeliveryDate());
        }

        if (request.getStatus() != null) {
            existingDelivery.setStatus(request.getStatus());
        }

        // Handle product updates and adjust inventory
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            Map<Product, Integer> productQuantities = request.getProducts().entrySet().stream()
                    .map(entry -> {
                        Product product = productRepository.findById(entry.getKey())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
                        return Map.entry(product, entry.getValue());
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Adjust inventory for removed products first (decrease inventory)
            existingDelivery.getProductQuantities().forEach((product, oldQuantity) -> {
                if (!productQuantities.containsKey(product)) {
                    // If the product is no longer in the delivery, adjust inventory (increase quantity)
                    adjustInventory(product, oldQuantity);  // Revert the inventory back
                } else {
                    // If the product is still in the delivery, adjust inventory based on the difference
                    int newQuantity = productQuantities.get(product);
                    int quantityDifference = oldQuantity - newQuantity;
                    adjustInventory(product, quantityDifference);  // Decrease inventory for removed quantity
                }
            });

            // Add or update products in the delivery (update inventory for added products)
            productQuantities.forEach((product, newQuantity) -> {
                Integer currentQuantity = existingDelivery.getQuantity(product);
                if (currentQuantity == null) {
                    currentQuantity = 0;  // Default to 0 if null
                }
                int quantityDifference = newQuantity - currentQuantity;
                adjustInventory(product, quantityDifference) ; // Decrease inventory for newly added quantity
                existingDelivery.addProduct(product, newQuantity);
            });

            // After updating product quantities, update the total price
            existingDelivery.updateTotalPrice();
        }

        return existingDelivery;
    }

    @Transactional
    @Override
    public Delivery overwriteUpdate(DeliveryUpdateRequest request, Long deliveryId) {
        System.out.println("Starting overwrite update for deliveryId: " + deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .map(existingDelivery -> {
                    System.out.println("Found existing delivery: " + existingDelivery);

                    Delivery updatedDelivery = overwriteDelivery(existingDelivery, request);
                    System.out.println("Updated delivery details: " + updatedDelivery);
                    return updatedDelivery;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for id: " + deliveryId));

        System.out.println("Saving updated delivery: " + delivery);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        System.out.println("Saved delivery to the database: " + savedDelivery);

        return savedDelivery;
    }

    private Delivery overwriteDelivery(Delivery existingDelivery, DeliveryUpdateRequest request) {
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
            existingDelivery.setUser(user);
        } else {
            existingDelivery.setUser(null); // Remove user if not provided
        }

        if (request.getMagacinId() != null) {
            Magacin magacin = magacinRepository.findById(request.getMagacinId())
                    .orElseThrow(() -> new ResourceNotFoundException("Magacin not found!"));
            existingDelivery.setMagacin(magacin);
        } else {
            existingDelivery.setMagacin(null); // Remove magacin if not provided
        }

        existingDelivery.setDeliveryDate(request.getDeliveryDate());

        // Overwrite product quantities entirely
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            Map<Product, Integer> productQuantities = request.getProducts().entrySet().stream()
                    .map(entry -> {
                        Product product = productRepository.findById(entry.getKey())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
                        product.setInventory(product.getInventory() + entry.getValue());
                        productRepository.save(product);
                        return Map.entry(product, entry.getValue());
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Clear current products and replace with new ones
            existingDelivery.getProductQuantities().forEach((product, quantity) -> {
                adjustInventory(product, quantity);  // Increase inventory for products being removed
            });

            // Add new products to delivery and adjust inventory
            existingDelivery.setProductQuantities(new HashMap<>(productQuantities));
            existingDelivery.updateTotalPrice();
        } else {
            // If no products are provided, clear the existing products
            existingDelivery.setProductQuantities(new HashMap<>());
            existingDelivery.getProducts().clear();
            existingDelivery.setTotalPrice(BigDecimal.ZERO); // Reset total price if no products
        }

        if (request.getStatus() != null) {
            existingDelivery.setStatus(request.getStatus());
        }

        return existingDelivery;
    }
}