package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.ProductDto;
import dev.olaxomi.backend.request.NewProductPriceRequest;
import dev.olaxomi.backend.request.NewProductRequest;
import dev.olaxomi.backend.request.UpdateProductRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.ProductPriceService;
import dev.olaxomi.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequestMapping("/v1/product")
@RestController
public class ProductController {
    private final ProductService productService;
    private final ProductPriceService productPriceService;

    public ProductController(ProductService productService, ProductPriceService productPriceService) {
        this.productService = productService;
        this.productPriceService = productPriceService;
    }

    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> all(){
        List<ProductDto> products = productService.allProducts();
        return ResponseEntity.ok(new MessageResponse("success", products));
    }

    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping("/{productId}")
    public ResponseEntity<MessageResponse> getProduct(@PathVariable Long productId){
        try{
            ProductDto product = productService.getProductById(productId);
            return ResponseEntity.ok(new MessageResponse("success", product));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    @PostMapping("/new")
    public ResponseEntity<MessageResponse> newProduct(@RequestBody
    NewProductRequest request){
        try{
            ProductDto product = productService.newProduct(request);
            return ResponseEntity.ok(new MessageResponse("success", product));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PostMapping("/{productId}/price/update")
    public ResponseEntity<MessageResponse> updatePrice(@PathVariable Long productId, @RequestBody NewProductPriceRequest request){
        try{
            ProductDto product = productPriceService.newProductPrice(request, productId);
            return ResponseEntity.ok(new MessageResponse("success", product));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping("/{productId}/update")
    public ResponseEntity<MessageResponse> updateProduct(@PathVariable Long productId, @RequestBody UpdateProductRequest request){
        try{
            ProductDto product = productService.updateProduct(request, productId);
            return ResponseEntity.ok(new MessageResponse("success", product));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PostMapping("/{productId}/renew")
    public ResponseEntity<MessageResponse> renewProduct(@PathVariable Long productId){
        try{
            ProductDto product = productService.renewProduct(productId);
            return ResponseEntity.ok(new MessageResponse("success", product));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }
}
