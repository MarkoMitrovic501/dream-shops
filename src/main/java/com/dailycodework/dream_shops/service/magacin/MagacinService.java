package com.dailycodework.dream_shops.service.magacin;

import com.dailycodework.dream_shops.exceptions.AlreadyExistsException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.model.User;
import com.dailycodework.dream_shops.repository.MagacinRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import com.dailycodework.dream_shops.repository.UserRepository;
import com.dailycodework.dream_shops.request.AddMagacinRequest;
import com.dailycodework.dream_shops.service.RefactorDependencies;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MagacinService implements IMagacinService {

    private final RefactorDependencies refactorDependencies;
    private final MagacinRepository magacinRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Override
    public Magacin getMagacin(Long id) {
        return magacinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Magacin not found"));
    }

    @Transactional
    @Override
    public Magacin clearMagacin(Long id) {
        Magacin magacin = getMagacin(id);
        for (Product product: magacin.getItems()) {
            product.setMagacin(null);
            productRepository.save(product);
        }
        magacin.getItems().clear();
        return magacinRepository.save(magacin);
    }
    @Override
    public Magacin createMagacin(AddMagacinRequest request) {
        if (request.getMagacin_id() == null) {
            throw new IllegalArgumentException("Magacin ID cannot be null");
        }

        if (magacinRepository.existsById(request.getMagacin_id())) {
            throw new AlreadyExistsException("Magacin with ID " + request.getMagacin_id() + " already exists.");
        }

        Magacin magacin = new Magacin();
        magacin.setId(request.getMagacin_id());
        magacin.setName(request.getMagacin_name());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
            magacin.setUser(user);
        }
        return magacinRepository.save(magacin);
    }

    @Transactional
    @Override
    public Magacin addProductToMagacin(Long magacinId, Long productId) {
        Magacin magacin = getMagacin(magacinId);
        Product product = refactorDependencies.getProductById(productId);
        product.setMagacin(magacin);
        productRepository.save(product);

        magacin.getItems().add(product);
        return magacinRepository.save(magacin);
    }
    @Override
    public Magacin initializeNewMagacin(User user) {
        return Optional.ofNullable(getMagacinByUserId(user.getId()))
                .orElseGet(() -> {
                    Magacin magacin = new Magacin();
                    magacin.setUser(user);
                    return magacinRepository.save(magacin);
                });
    }

    @Override
    public Magacin deleteMagacinById(Long magacinId) {
        Magacin magacin = getMagacin(magacinId);
        magacinRepository.delete(magacin);
        return magacin;
    }

    @Override
    public Magacin getProductById(Long productId) {
        return null;
    }
    @Override
    public Optional<Magacin> getMagacinByIdAndUser(Long magacinId, User user) {
        return magacinRepository.findByIdAndUser(magacinId, user);
    }

    @Override
    public Set<Product> countUniqueProductsInMagacin(Long magacinId) {
        Magacin magacin = getMagacin(magacinId);
        return magacin.getItems();
    }

    @Override
    public Magacin findMagacinById(Long magacinId) {
        return null;
    }

    public Magacin getMagacinByUserId(Long userId) {
        return magacinRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Magacin not found for user with id: " + userId));
    }
}