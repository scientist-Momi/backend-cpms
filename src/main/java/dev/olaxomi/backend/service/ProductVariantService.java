package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ProductVariantDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.ProductMapper;
import dev.olaxomi.backend.mapper.ProductVariantMapper;
import dev.olaxomi.backend.model.Product;
import dev.olaxomi.backend.model.ProductVariant;
import dev.olaxomi.backend.repository.ProductRepository;
import dev.olaxomi.backend.repository.ProductVariantRepository;
import dev.olaxomi.backend.request.CreateUpdateProductVariantRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;
    private final AdminActivityService activityService;

    public ProductVariantService(ProductVariantRepository productVariantRepository, ProductRepository productRepository, ProductMapper productMapper, ProductVariantMapper variantMapper, AdminActivityService activityService) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.variantMapper = variantMapper;
        this.activityService = activityService;
    }

    public List<ProductVariantDto> getVariantsByProduct(Long productId) {
        return variantMapper.toDtoList(productVariantRepository.findByProductId(productId));
    }

    public ProductVariantDto createVariant(Long productId, CreateUpdateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        boolean duplicate = productVariantRepository.existsByProductIdAndWeight(productId, request.getWeight());
        if (duplicate) {
            throw new IllegalArgumentException("Variant with weight " + request.getWeight() + "Kg already exists for this product.");
        }

        ProductVariant variant = new ProductVariant();
        variant.setWeight(request.getWeight());
        variant.setInventory(request.getInventory());
        variant.setProduct(product);

        ProductVariant savedVariant = productVariantRepository.save(variant);

        String logDetails = String.format(
                "Created new variant for product '%s' (Product ID: %d) → Variant ID: %d, Weight: %.2fKg, Inventory: %d",
                product.getName(),
                product.getId(),
                savedVariant.getId(),
                savedVariant.getWeight(),
                savedVariant.getInventory()
        );

        activityService.logActivity(
                ActionType.CREATE_VARIANT, // ✅ Recommend adding this to enum; or use CREATE_PRODUCT if no variant-specific type
                TargetType.PRODUCT_VARIANT, // ✅ Recommend adding; or keep TargetType.PRODUCT
                String.valueOf(savedVariant.getId()),
                logDetails
        );
        return variantMapper.toDto(savedVariant);
    }

    public ProductVariantDto updateVariant(Long productId, Long variantId, CreateUpdateProductVariantRequest request) {
        if(variantId == null){
            throw new IllegalArgumentException("No variants created for this product yet.");
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));

        boolean exists = productVariantRepository.existsByProductIdAndWeightAndIdNot(
                variant.getProduct().getId(),
                request.getWeight(),
                variantId
        );
        if (exists) {
            throw new IllegalArgumentException("Duplicate weight for this product");
        }

        variant.setWeight(request.getWeight());
        variant.setInventory(request.getInventory());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return variantMapper.toDto(savedVariant);
    }

    public void deleteVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new EntityNotFoundException("Variant not found");
        }
        productVariantRepository.deleteById(variantId);
    }
}
