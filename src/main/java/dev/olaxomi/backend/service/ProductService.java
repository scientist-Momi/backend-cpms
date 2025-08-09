package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ProductDto;
import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.ProductMapper;
import dev.olaxomi.backend.model.Product;
import dev.olaxomi.backend.model.ProductPrice;
import dev.olaxomi.backend.repository.ProductPriceRepository;
import dev.olaxomi.backend.repository.ProductRepository;
import dev.olaxomi.backend.request.NewProductRequest;
import dev.olaxomi.backend.request.UpdateProductRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductMapper productMapper;
    private final AdminActivityService activityService;

    public ProductService(ProductRepository productRepository, ProductPriceRepository productPriceRepository, ProductMapper productMapper, AdminActivityService activityService) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productMapper = productMapper;
        this.activityService = activityService;
    }

    public List<ProductDto> allProducts() {
        List<Product> initProducts = productRepository.findAllByOrderByCreatedAtDesc();
        return productMapper.toDtoList(initProducts);
    }

    public ProductDto getProductById(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
        return productMapper.toDto(product);
    }

    public ProductDto newProduct(NewProductRequest request){
        if(productRepository.existsByName(request.getName())){
            throw new RuntimeException("Product name already exists.");
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setInventory(request.getInventory());
        product.setQuantitySold(0);
        product.setDescription(request.getDescription());
        Product savedProduct = productRepository.save(product);

        ProductPrice price = new ProductPrice();
        price.setProduct(savedProduct);
        price.setPrice(request.getPrice());
        price.setCreatedAt(LocalDateTime.now());
        price = productPriceRepository.save(price);

        // 4. Log product creation with initial price
        String logDetails = String.format(
                "Created new product '%s' (ID: %d) with initial price %.2f at %s",
                savedProduct.getName(),
                savedProduct.getId(),
                price.getPrice(),
                price.getCreatedAt()
        );

        activityService.logActivity(
                ActionType.CREATE_PRODUCT,
                TargetType.PRODUCT,
                savedProduct.getId().toString(),
                logDetails
        );
        return productMapper.toDto(savedProduct);
    }

    public ProductDto updateProduct(UpdateProductRequest request, Long productId){
        return productRepository.findById(productId).map(existingProduct -> {
            existingProduct.setName(request.getName());
            existingProduct.setInventory(request.getInventory());
            existingProduct.setBrand(request.getBrand());
            existingProduct.setDescription(request.getDescription());
            Product savedProduct = productRepository.save(existingProduct);

            String logDetails = String.format(
                    "Updated product '%s' (ID: %d): name set to '%s', inventory to %d, brand to '%s', description updated.",
                    savedProduct.getName(),
                    savedProduct.getId(),
                    savedProduct.getName(),
                    savedProduct.getInventory(),
                    savedProduct.getBrand()
            );
            activityService.logActivity(
                    ActionType.UPDATE_PRODUCT,
                    TargetType.PRODUCT,
                    savedProduct.getId().toString(),
                    logDetails
            );

            return productMapper.toDto(savedProduct);
        }).orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public ProductDto renewProduct(Long productId){
        ProductDto productToCopy = getProductById(productId);
        Product product = new Product();
        product.setName(productToCopy.getName() + " copy");
        product.setBrand(productToCopy.getBrand());
        product.setInventory(0);
        product.setQuantitySold(0);
        product.setDescription(productToCopy.getDescription());
        Product savedProduct = productRepository.save(product);

        ProductPrice price = new ProductPrice();
        price.setProduct(savedProduct);
        price.setPrice(productToCopy.getLatestPrice().getPrice());
        price.setCreatedAt(LocalDateTime.now());
        price = productPriceRepository.save(price);

        String logDetails = String.format(
                "Renewed product from template (original ID: %d). New product '%s' (ID: %d) with initial price %.2f at %s",
                productId,
                savedProduct.getName(),
                savedProduct.getId(),
                price.getPrice(),
                price.getCreatedAt()
        );
        activityService.logActivity(
                ActionType.RENEW_PRODUCT,
                TargetType.PRODUCT,
                savedProduct.getId().toString(),
                logDetails
        );

        return productMapper.toDto(savedProduct);
    }
}
