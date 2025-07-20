package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ProductDto;
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

    public ProductPriceService(ProductPriceRepository productPriceRepository, ProductRepository productRepository, ProductMapper productMapper) {
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
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
        productPriceRepository.save(productPrice);
        return productMapper.toDto(product);
    }
}
