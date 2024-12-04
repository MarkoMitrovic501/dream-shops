package com.dailycodework.dream_shops.service.magacin;

import com.dailycodework.dream_shops.exceptions.AlreadyExistsException;
import com.dailycodework.dream_shops.exceptions.InsufficientStockException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.repository.MagacinRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import com.dailycodework.dream_shops.repository.UserRepository;
import com.dailycodework.dream_shops.request.AddMagacinRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MagacinService implements IMagacinService {
    private static final Logger logger = LoggerFactory.getLogger(MagacinService.class);


    private final MagacinRepository magacinRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Override
    public Magacin getMagacin(Long id) {
        logger.info("Fetching magacin with id: {}", id);
        return magacinRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Magacin not found with ID: {}", id);
                    return new ResourceNotFoundException("Magacin not found");
                });
    }

    @Transactional
    @Override
    public void clearMagacin(Long id) {
        logger.info("Clearing magacin with id: {}", id);
        Magacin magacin = getMagacin(id);
        for (Product product: magacin.getItems()) {
            logger.info("Deleting product with id: {}", product.getId());
            product.setMagacin(null);
            productRepository.save(product);
        }
        magacin.getItems().clear();
        magacinRepository.save(magacin);
        logger.info("Magacin has been cleared");
    }
    @Override
    public Magacin createMagacin(AddMagacinRequest request) {
        logger.info("Creating new magacin with ID: {}",request.getMagacin_id());

        if (request.getMagacin_id() == null) {
            logger.error("Magacin ID cannot be null");
            throw new IllegalArgumentException("Magacin ID cannot be null");
        }

        if (magacinRepository.existsById(request.getMagacin_id())) {
            logger.warn("Magacin with ID {} already exists", request.getMagacin_id());
            throw new AlreadyExistsException("Magacin with ID " + request.getMagacin_id() + " already exists.");
        }

        Magacin magacin = new Magacin();
        magacin.setId(request.getMagacin_id());
        magacin.setName(request.getMagacin_name());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> {
                        logger.warn("User not found with ID: {}", request.getUserId());
                        return new ResourceNotFoundException("User not found with ID: " + request.getUserId());
                    });
            magacin.setUser(user);
        }

        Magacin savedMagacin = magacinRepository.save(magacin);
        logger.info("Magacin created successfully with ID: {}", savedMagacin.getId());
        return savedMagacin;
    }

    public void adjustInventory(Product product, int quantityDifference) {
        logger.info("Adjusting inventory for product: {}", product.getName());

        int newInventory = product.getInventory() + quantityDifference;

        if (newInventory < 0) {
            logger.error("Insufficient stock for product: {}. Available: {}, Tried to reduce by: {}",
                    product.getName(), product.getInventory(), -quantityDifference);
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                            ". Available: " + product.getInventory() + ", Tried to reduce by: " + (-quantityDifference));
        }

        product.setInventory(newInventory);
        productRepository.save(product);
        logger.info("Inventory adjusted successfully for product: {}. New inventory: {}", product.getName(), newInventory);
    }

    @Override
    public void addProductToMagacin(Long id, Long productId, int quantity) {
        logger.info("Adding product with ID: {} to Magacin with ID: {}", productId, id);

        Magacin magacin = this.getMagacin(id);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found");
                });

        if (product.getInventory() >= quantity) {
            if (!magacin.getItems().contains(product)) {
                logger.debug("Product {} added to Magacin {}", product.getName(), magacin.getName());
                magacin.addItem(product);
            }

            adjustInventory(product, -quantity);
            magacinRepository.save(magacin);
            logger.info("Product added successfully to Magacin with ID: {}", id);
        } else {
            logger.error("Insufficient inventory for product: {}. Available: {}, Required: {}",
                    product.getName(), product.getInventory(), quantity);
            throw new InsufficientStockException("Insufficient inventory for product: " + product.getName());
        }
    }

    @Override
    public void deleteMagacinById(Long magacinId) {
        logger.info("Deleting Magacin with ID: {}", magacinId);
        Magacin magacin = getMagacin(magacinId);
        magacinRepository.delete(magacin);
        logger.info("Magacin deleted successfully.");
    }

    @Override
    public Set<Product> countUniqueProductsInMagacin(Long magacinId) {
        logger.info("Counting unique products in Magacin with ID: {}", magacinId);
        Magacin magacin = getMagacin(magacinId);
        return magacin.getItems();
    }

    public Magacin getMagacinByUserId(Long userId) {
        logger.info("Fetching Magacin for user with ID: {}", userId);
        return magacinRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Magacin not found for user with ID: {}", userId);
                    return new ResourceNotFoundException("Magacin not found for user with ID: " + userId);
                });
    }
}