package com.dailycodework.dream_shops.controller;

import com.dailycodework.dream_shops.exceptions.AlreadyExistsException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.request.AddMagacinRequest;
import com.dailycodework.dream_shops.request.AddProductToMagacinRequest;
import com.dailycodework.dream_shops.response.ApiResponse;
import com.dailycodework.dream_shops.service.magacin.IMagacinService;
import com.dailycodework.dream_shops.service.product.ProductService;
import com.dailycodework.dream_shops.service.user.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/magacin")
public class MagacinController {

    private final IMagacinService magacinService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/allProduct")
    public ResponseEntity<ApiResponse> getAllProductInMagacin() {
        List<Magacin> products = productService.getAllProductInMagacin();

        return ResponseEntity.ok(new ApiResponse("success", products));
    }

    @PostMapping("/createMagacin")
    public ResponseEntity<ApiResponse> createMagacin(@RequestBody AddMagacinRequest request){
        try {
            Magacin theMagacin = magacinService.createMagacin(request);
            return ResponseEntity.ok(new ApiResponse("Add magacin successfully", theMagacin));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/addProduct")
    public ResponseEntity<ApiResponse> addProductToMagacin(@RequestBody AddProductToMagacinRequest request) {
    try {
        System.out.println("Test");
        User user = userService.getAuthenticatedUser();
        System.out.println("Authenticated User ID: " + user.getId());

        Magacin magacin = magacinService.getMagacin(request.getMagacinId());
        if (magacin == null) {
            throw new ResourceNotFoundException("Magacin with ID " + request.getMagacinId() + " not found.");
        }
        System.out.println("Magacin: " + magacin);
        System.out.println("Magacin Owner User ID: " + magacin.getUser().getId());

        if (magacin.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Magacin with ID " + request.getMagacinId() + " does not belong to the authenticated user.");
        }

        System.out.println("Adding product with ID " + request.getProductId());
        magacinService.addProductToMagacin(request.getMagacinId(), request.getProductId(), request.getQuantity());

        Magacin updatedMagacin = magacinService.getMagacin(request.getMagacinId());
        return ResponseEntity.ok(new ApiResponse("Add Item Success", updatedMagacin));

    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    } catch (JwtException e) {
        return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
    } catch (Exception e) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Unexpected error occurred", null));
       }
    }

    @GetMapping("/{magacinId}")
    public ResponseEntity<ApiResponse> getMagacin( @PathVariable Long magacinId) {
        try {
            Magacin magacin = magacinService.getMagacin(magacinId);
            return ResponseEntity.ok(new ApiResponse("Success", magacin));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{magacinId}/countUniqueProducts")
    public ResponseEntity<ApiResponse> countUniqueProductsInMagacin(@PathVariable Long magacinId) {
        try {
            Set<Product> uniqueProductCount = magacinService.countUniqueProductsInMagacin(magacinId);

            return ResponseEntity.ok(new ApiResponse("Count Success", uniqueProductCount));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{magacinId}/clear")
    public ResponseEntity<ApiResponse> clearMagacin( @PathVariable Long magacinId) {
        try {
            magacinService.clearMagacin(magacinId);
            return ResponseEntity.ok(new ApiResponse("Clear Magacin Success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{magacinId}/delete")
    public ResponseEntity<ApiResponse> deleteMagacinById( @PathVariable Long magacinId) {
        try {
            magacinService.deleteMagacinById(magacinId);
            return ResponseEntity.ok(new ApiResponse("Delete Magacin Success!", magacinId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}