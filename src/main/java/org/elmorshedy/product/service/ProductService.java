package org.elmorshedy.product.service;

import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductUpdateRequest;
import org.elmorshedy.product.repo.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public Product getProduct(ObjectId id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    //done
    public Product CreateProduct(Product product) {
        return productRepo.save(product);
    }
    //done
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public void deleteProduct(ObjectId id) {
        if (!productRepo.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepo.deleteById(id);
    }

    public Product editamount(ObjectId id, int amount) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setAmount(amount);
        return productRepo.save(product);
    }
    //done
    public Product updateProduct(ObjectId id, ProductUpdateRequest product) {
        Product product1 = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (product1.getDescription() != null) {
            product1.setDescription(product.getDescription());
        }
        if (product.getName() != null) {
            product1.setName(product.getName());
        }
        if (product.getPrice() != 0) {
            product1.setPrice(product.getPrice());
        }
        if (product.getAmount() != 0) {
            product1.setAmount(product.getAmount());
        }
        return productRepo.save(product1);
    }
}
