package org.elmorshedy.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductDTO;
import org.elmorshedy.product.model.ProductUpdateRequest;

import org.elmorshedy.product.service.ProductService;
import org.elmorshedy.security.ObjectIdParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operations related to product management")

public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new product", description = "Admins only can create products")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.CreateProduct(product));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products from the system")
    @ApiResponse(responseCode = "200", description = "List of products",
            content = @Content(schema = @Schema(implementation = ProductDTO.class)))
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID", description = "Retrieve product details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductDTO> getProduct(
            @Parameter(description = "ID of the product to retrieve") @ObjectIdParam @PathVariable ObjectId id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Admins only can delete products")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete")@ObjectIdParam @PathVariable ObjectId id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/amount")
    @Operation(summary = "Update product amount", description = "Update only the stock amount of a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amount updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductDTO> updateAmount(
            @Parameter(description = "ID of the product to update")@ObjectIdParam @PathVariable ObjectId id,
            @Valid @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.editamount(id, product.getAmount()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Update a product", description = "Admins only can update product details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID of the product to update")@ObjectIdParam @PathVariable ObjectId id,
            @RequestBody ProductUpdateRequest product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }
}
