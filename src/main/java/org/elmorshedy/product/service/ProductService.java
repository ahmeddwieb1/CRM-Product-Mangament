package org.elmorshedy.product.service;

import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductDTO;
import org.elmorshedy.product.model.ProductUpdateRequest;
import org.elmorshedy.product.repo.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public ProductDTO getProduct(ObjectId id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
        return new ProductDTO(product);
    }

    public Product CreateProduct(Product product) {
        return productRepo.save(product);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepo.findAll()
                .stream()
                .map(ProductDTO::new)
                .toList();
    }

    public void deleteProduct(ObjectId id) {
        if (!productRepo.existsById(id)) {
            throw new NoSuchElementException("Product not found with id: " + id);
        }
        productRepo.deleteById(id);
    }

    public ProductDTO editamount(ObjectId id, int amount) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
        product.setAmount(amount);
        return new ProductDTO(productRepo.save(product));
    }

    public ProductDTO updateProduct(ObjectId id, ProductUpdateRequest product) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));

        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getName() != null && !product.getName().isBlank()) {
            existingProduct.setName(product.getName());
        }
        if (product.getPrice() != null && product.getPrice() > 0) {
            existingProduct.setPrice(product.getPrice());
        }
        // تحقّق من null قبل المقارنة للـ amount
        if (product.getAmount() != null && product.getAmount() >= 0) {
            existingProduct.setAmount(product.getAmount());
        }

        Product saved = productRepo.save(existingProduct);
        return new ProductDTO(saved);
    }

}
