package org.elmorshedy.product.controller;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductUpdateRequest;
import org.elmorshedy.product.repo.ProductRepo;
import org.elmorshedy.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
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
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable ObjectId id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable ObjectId id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/amount")
    public ResponseEntity<Product> updateamount(@PathVariable ObjectId id, @Valid @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.editamount(id, product.getAmount()));
    }

        @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable ObjectId id, @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }
}
