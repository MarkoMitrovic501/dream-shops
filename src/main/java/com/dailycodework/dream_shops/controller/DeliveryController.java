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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/deliveries")
public class DeliveryController {



    private final IDeliveryService deliveryService;

    @PostMapping(value = "/createDelivery", consumes = "application/json")
    public ResponseEntity<ApiResponse> createDelivery(@RequestBody CreateDeliveryRequest request) {
        try {
            Long userId = request.getUserId();
            Long productId = request.getProductId();
            int quantity = request.getQuantity();
      // LocalDate deliveryDate = request.getDeliveryDate();
            System.out.println("createDelivery 1 - Request received");
            System.out.println("UserId: " + userId + ", ProductId: " + productId + ", Quantity: " + quantity);

            if (userId == null || productId == null || quantity <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Error", "Invalid input data. Please ensure userId, productId are provided, and quantity is greater than 0."));
            }
            System.out.println("createDelivery 2");
            Delivery delivery = deliveryService.placeDelivery(userId);

            deliveryService.addProductToDelivery(delivery.getId(), productId, quantity);
            System.out.println("createDelivery 3");
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);
            System.out.println("createDelivery 4");
            return ResponseEntity.ok(new ApiResponse("Success", deliveryDto));

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Error", "User not found for userId: " + request.getUserId()));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Error", "Invalid input: " + ex.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/{deliveryId}/products")
    public ResponseEntity<ApiResponse> addProductToDelivery(@PathVariable Long deliveryId,
                                                            @RequestParam Long productId,
                                                            @RequestParam int quantity) {
        try {
            if (deliveryId == null || productId == null || quantity <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Error", "Invalid input data"));
            }
            deliveryService.addProductToDelivery(deliveryId, productId, quantity);
            Delivery delivery = deliveryService.getDelivery(deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(delivery);

            return ResponseEntity.ok(new ApiResponse("Success", deliveryDto));
        } catch (InsufficientStockException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Error", ex.getMessage()));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Error", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

        @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse> getDelivery( @PathVariable Long deliveryId) {
        try {
            Delivery delivery = deliveryService.getDelivery(deliveryId);
            return ResponseEntity.ok(new ApiResponse("Success", delivery));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse> updateDelivery(@RequestBody DeliveryUpdateRequest request, @PathVariable Long deliveryId) {
        try {
            Delivery updatedDelivery = deliveryService.updateDelivery(request, deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            return ResponseEntity.ok(new ApiResponse("Update delivery success;", deliveryDto));
    } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @PutMapping("/{deliveryId}/overwriteDelivery")
    public ResponseEntity<ApiResponse> overwriteDelivery(@RequestBody DeliveryUpdateRequest request, @PathVariable Long deliveryId) {
        try {
            Delivery updatedDelivery = deliveryService.overwriteUpdate(request, deliveryId);
            DeliveryDto deliveryDto = deliveryService.convertToDto(updatedDelivery);
            return ResponseEntity.ok(new ApiResponse("Update delivery success;", deliveryDto));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<String> deleteDelivery(@PathVariable Long deliveryId) {
        try {
            deliveryService.deleteDelivery(deliveryId);

            return ResponseEntity.ok("Delivery with id " + deliveryId + " deleted successfully.");
        } catch (ResourceNotFoundException ex) {

            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {

            return ResponseEntity.status(500).body("Error deleting delivery: " + ex.getMessage());
        }
    }
}