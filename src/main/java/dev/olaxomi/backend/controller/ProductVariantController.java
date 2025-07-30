package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.ProductVariantDto;
import dev.olaxomi.backend.model.ProductVariant;
import dev.olaxomi.backend.request.CreateUpdateProductVariantRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.ProductVariantService;
import jakarta.validation.constraints.Null;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/v1/products/{productId}/variants")
public class ProductVariantController {
    private final ProductVariantService variantService;

    public ProductVariantController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping
    public ResponseEntity<MessageResponse> getVariants(@PathVariable Long productId) {
        List<ProductVariantDto> variants = variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(new MessageResponse("success", variants));
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PostMapping
    public ResponseEntity<MessageResponse> createVariant(@PathVariable Long productId,
                                           @RequestBody CreateUpdateProductVariantRequest request) {
        try{
            ProductVariantDto variant = variantService.createVariant(productId, request);
            return ResponseEntity.ok(new MessageResponse("success", variant));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping("/{variantId}")
    public ResponseEntity<MessageResponse> updateVariant(@PathVariable Long productId, @PathVariable Long variantId,
                                           @RequestBody CreateUpdateProductVariantRequest request) {
        try{
            ProductVariantDto variant = variantService.updateVariant(productId, variantId, request);
            return ResponseEntity.ok(new MessageResponse("success", variant));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @DeleteMapping("/{variantId}")
    public ResponseEntity<MessageResponse> deleteVariant(@PathVariable Long variantId) {
        try{
        variantService.deleteVariant(variantId);
            return ResponseEntity.ok(new MessageResponse("success", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }
}
