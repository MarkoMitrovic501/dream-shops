package com.dailycodework.dream_shops.service.product;

import com.dailycodework.dream_shops.dto.ProductDto;
import com.dailycodework.dream_shops.exceptions.AlreadyExistsException;
import com.dailycodework.dream_shops.exceptions.ResourceNotFoundException;
import com.dailycodework.dream_shops.model.Category;
import com.dailycodework.dream_shops.model.Magacin;
import com.dailycodework.dream_shops.model.Product;
import com.dailycodework.dream_shops.repository.CategoryRepository;
import com.dailycodework.dream_shops.repository.MagacinRepository;
import com.dailycodework.dream_shops.repository.ProductRepository;
import com.dailycodework.dream_shops.request.AddProductRequest;
import com.dailycodework.dream_shops.request.ProductUpdateRequest;
import com.dailycodework.dream_shops.service.RefactorDependencies;
import com.dailycodework.dream_shops.service.magacin.IMagacinService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final RefactorDependencies refactorDependencies;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final MagacinRepository magacinRepository;
    @Getter
    private final IMagacinService magacinService;

    @Override
    public Product addProduct(AddProductRequest request) {
        if (productExist(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException(request.getBrand() + " " + request.getName() + " already exists, you may update this product instead!");
        }

        Category category = categoryRepository.findByName(request.getCategory().getName());
        if (category == null) {
            category = new Category(request.getCategory().getName());
            categoryRepository.save(category);
        }

        request.setCategory(category);
        return productRepository.save(createProduct(request, category));
    }

    private boolean productExist(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        return refactorDependencies.getProductById(id);

    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete,
                () -> { throw new ResourceNotFoundException("Product not found!"); });
    }

    @Override
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    @Override
    public Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        if (request.getName() != null) {
            existingProduct.setName(request.getName());
        }
        if (request.getBrand() != null) {
            existingProduct.setBrand(request.getBrand());
        }
        if (request.getPrice() != null) {
            existingProduct.setPrice(request.getPrice());
        }

        if (request.getDescription() != null) {
            existingProduct.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findByName(request.getCategory().getName());
            existingProduct.setCategory(category);
        }

        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {

        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public void addProductToMagacin(Long id, Long productId) {
        Magacin magacin = magacinService.getMagacin(id);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!magacin.getItems().contains(product)) {
            magacin.addItem(product);
            magacinRepository.save(magacin);
        }
    }

    @Override
    public List<Magacin> getAllProductInMagacin() {
        return magacinRepository.findAll();
    }
}