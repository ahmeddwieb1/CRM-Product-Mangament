package org.elmorshedy.product.controller;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductDTO;
import org.elmorshedy.product.model.ProductUpdateRequest;

import org.elmorshedy.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.CreateProduct(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable ObjectId id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable ObjectId id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/amount")
    public ResponseEntity<ProductDTO> updateamount(@PathVariable ObjectId id,
                                                @Valid @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.editamount(id, product.getAmount()));
    }

        @PatchMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable ObjectId id,
                                                 @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }
}
