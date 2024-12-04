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
import com.dailycodework.dream_shops.service.magacin.IMagacinService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final MagacinRepository magacinRepository;
    @Getter
    private final IMagacinService magacinService;

    @Override
    public Product addProduct(AddProductRequest request) {
        logger.info("Adding a new product: {} - {}", request.getBrand(), request.getName());

        if (productExist(request.getName(), request.getBrand())) {
            logger.warn("Product already exists: {} - {}", request.getBrand(), request.getName());
            throw new AlreadyExistsException(request.getBrand() + " " + request.getName() + " already exists, you may update this product instead!");
        }

        Category category = categoryRepository.findByName(request.getCategory().getName());
        if (category == null) {
            logger.info("Category not found. Creating a new category: {}", request.getCategory().getName());
            category = new Category(request.getCategory().getName());
            categoryRepository.save(category);
        }

        request.setCategory(category);
        Product savedProduct = productRepository.save(createProduct(request, category));
        logger.info("Product added successfully with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    private boolean productExist(String name, String brand) {
        boolean exists = productRepository.existsByNameAndBrand(name, brand);
        logger.debug("Checking if product exists: {} - {}. Result: {}", name, brand, exists);
        return exists;
    }

    private Product createProduct(AddProductRequest request, Category category) {
        logger.debug("Creating product object for: {} - {}", request.getBrand(), request.getName());
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
        logger.info("Fetching product by ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for ID: " + id));
    }


    @Override
    public void deleteProductById(Long id) {
        logger.info("Deleting product by ID: {}", id);
        productRepository.findById(id).ifPresentOrElse(product -> {
            productRepository.delete(product);
            logger.info("Product deleted successfully with ID: {}", id);
        }, () -> {
            logger.warn("Product not found for deletion with ID: {}", id);
            throw new ResourceNotFoundException("Product not found!");
        });
    }

    @Override
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        logger.info("Updating product with ID: {}", productId);
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for ID: " + productId));
    }


    @Override
    public Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        logger.debug("Updating product fields for ID: {}", existingProduct.getId());

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

        logger.info("Product updated successfully with ID: {}", existingProduct.getId());
        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        logger.info("Fetching products by brand: {}", brand);
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        logger.info("Fetching products by category: {} and brand: {}", category, brand);
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        logger.info("Fetching products by name: {}", name);
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        logger.info("Fetching products by brand: {} and name: {}", brand, name);
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        logger.info("Counting products by brand: {} and name: {}", brand, name);
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        logger.debug("Converting product list to DTO");
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        logger.debug("Converting product to DTO for ID: {}", product.getId());
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public List<Magacin> getAllProductInMagacin() {
        logger.info("Fetching all products in magacin");
        return magacinRepository.findAll();
    }
}