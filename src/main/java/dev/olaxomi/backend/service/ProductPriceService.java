package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ProductDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.ProductMapper;
import dev.olaxomi.backend.model.Product;
import dev.olaxomi.backend.model.ProductPrice;
import dev.olaxomi.backend.repository.ProductPriceRepository;
import dev.olaxomi.backend.repository.ProductRepository;
import dev.olaxomi.backend.request.NewProductPriceRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductPriceService {
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AdminActivityService activityService;

    public ProductPriceService(ProductPriceRepository productPriceRepository, ProductRepository productRepository, ProductMapper productMapper, AdminActivityService activityService) {
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.activityService = activityService;
    }

    public List<ProductPrice> allPriceForAProduct(Long productId){
        List<ProductPrice> initProductPrices = new ArrayList<>();
        productPriceRepository.findAll().forEach(initProductPrices::add);
        return initProductPrices;
    }

    public ProductDto newProductPrice(NewProductPriceRequest request, Long productId){
        ProductPrice productPrice = new ProductPrice();
        productPrice.setPrice(request.getPrice());
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found!"));
        productPrice.setProduct(product);
        productPrice.setCreatedAt(LocalDateTime.now());
        productPrice = productPriceRepository.save(productPrice);

        String logDetails = String.format(
                "Added new price %.2f for product ID %d at %s",
                productPrice.getPrice(),
                product.getId(),
                productPrice.getCreatedAt()
        );

        activityService.logActivity(
                ActionType.UPDATE_PRODUCT_PRICE,
                TargetType.PRODUCT,
                String.valueOf(product.getId()),
                logDetails
        );

        return productMapper.toDto(product);
    }
}
