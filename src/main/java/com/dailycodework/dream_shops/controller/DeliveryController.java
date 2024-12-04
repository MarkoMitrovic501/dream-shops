package com.dailycodework.dream_shops.controller;

import com.dailycodework.dream_shops.dto.DeliveryDto;
import com.dailycodework.dream_shops.exceptions.InsufficientStockException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Delivery;
import com.dailycodework.dream_shops.request.CreateDeliveryRequest;
import com.dailycodework.dream_shops.request.DeliveryUpdateRequest;
import com.dailycodework.dream_shops.response.ApiResponse;
import com.dailycodework.dream_shops.service.delivery.IDeliveryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/deliveries")
public class DeliveryController {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryController.class);

    private final IDeliveryService deliveryService;

    @PostMapping(value = "/createDelivery", consumes = "application/json")
    public ResponseEntity<ApiResponse> createDelivery(@RequestBody CreateDeliveryRequest request) {
        logger.info("Received createDelivery request: {}", request);
        try {
            Long userId = request.getUserId();
            Long productId = request.getProductId();
            int quantity = request.getQuantity();
            logger.debug("UserId: {}, ProductId: {}, Quantity: {}", userId, productId, quantity);

            if (userId == null || productId == null || quantity <= 0) {
                logger.warn("Invalid input data for createDelivery");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Error", "Invalid input data. Please ensure userId, productId are provided, and quantity is greater than 0."));
            }
            Delivery delivery = deliveryService.placeDelivery(userId);
            deliveryService.addProductToDelivery(delivery.getId(), productId, quantity);
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);
            logger.info("Delivery created successfully with ID: {}", delivery.getId());
            return ResponseEntity.ok(new ApiResponse("Success", deliveryDto));

        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Error", "User not found for userId: " + request.getUserId()));

        } catch (IllegalArgumentException ex) {
            logger.error("Invalid input: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Error", "Invalid input: " + ex.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error occurred during createDelivery: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/{deliveryId}/products")
    public ResponseEntity<ApiResponse> addProductToDelivery(@PathVariable Long deliveryId,
                                                            @RequestParam Long productId,
                                                            @RequestParam int quantity) {
        logger.info("Adding product to delivery: deliveryId={}, productId={}, quantity={}", deliveryId, productId, quantity);
        try {
            if (deliveryId == null || productId == null || quantity <= 0) {
                logger.warn("Invalid input data for addProductToDelivery");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Error", "Invalid input data"));
            }
            deliveryService.addProductToDelivery(deliveryId, productId, quantity);
            Delivery delivery = deliveryService.getDelivery(deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);
            logger.info("Product added successfully to delivery: {}", deliveryId);
            return ResponseEntity.ok(new ApiResponse("Success", deliveryDto));
        } catch (InsufficientStockException ex) {
            logger.error("Insufficient stock: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Error", ex.getMessage()));
        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Error", ex.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error occurred while adding product to delivery: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

        @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse> getDelivery( @PathVariable Long deliveryId) {
            logger.info("Fetching delivery with ID: {}", deliveryId);
            try {
            Delivery delivery = deliveryService.getDelivery(deliveryId);
                logger.info("Delivery retrieved successfully: {}", deliveryId);
                return ResponseEntity.ok(new ApiResponse("Success", delivery));
        } catch (ResourceNotFoundException e) {
                logger.error("Delivery not found: {}", e.getMessage());
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse> updateDelivery(@RequestBody DeliveryUpdateRequest request, @PathVariable Long deliveryId) {
        logger.info("Updating delivery with ID: {}", deliveryId);
        try {
            Delivery updatedDelivery = deliveryService.updateDelivery(request, deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            logger.info("Delivery updated successfully: {}", deliveryId);
            return ResponseEntity.ok(new ApiResponse("Update delivery success;", deliveryDto));
    } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @PutMapping("/{deliveryId}/overwriteDelivery")
    public ResponseEntity<ApiResponse> overwriteDelivery(@RequestBody DeliveryUpdateRequest request, @PathVariable Long deliveryId) {
        logger.info("Overwriting delivery with ID: {}", deliveryId);
        try {
            Delivery updatedDelivery = deliveryService.overwriteUpdate(request, deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            logger.info("Delivery overwritten successfully: {}", deliveryId);
            return ResponseEntity.ok(new ApiResponse("Overwrite delivery success", deliveryDto));
        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<String> deleteDelivery(@PathVariable Long deliveryId) {
        logger.info("Deleting delivery with ID: {}", deliveryId);
        try {
            deliveryService.deleteDelivery(deliveryId);
            logger.info("Delivery deleted successfully: {}", deliveryId);
            return ResponseEntity.ok("Delivery with id " + deliveryId + " deleted successfully.");
        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error deleting delivery: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting delivery: " + ex.getMessage());
        }
    }
}