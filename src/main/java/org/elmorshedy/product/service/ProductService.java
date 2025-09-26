package org.elmorshedy.product.service;

import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.model.ProductDTO;
import org.elmorshedy.product.model.ProductUpdateRequest;
import org.elmorshedy.product.repo.ProductRepo;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

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
        long start = System.currentTimeMillis();
        List<ProductDTO> result = productRepo.findAll()
                .stream()
                .map(ProductDTO::new)
                .toList();
        long end = System.currentTimeMillis();
        System.out.println("get all product: " + "Query time: " + (end - start) + "ms");
        return result;
    }

    public List<ProductDTO> getProductsWithPage(Pageable pageable) {
        long start = System.currentTimeMillis();
        List<ProductDTO> result = productRepo.findAllBy(pageable)
                .stream()
                .map(ProductDTO::new)
                .toList();
        long end = System.currentTimeMillis();
        System.out.println("get all product: " + "Query time: " + (end - start) + "ms");
        return result;
    }

    public void deleteProduct(ObjectId id) {
        long start = System.currentTimeMillis();
        if (!productRepo.existsById(id)) {
            throw new NoSuchElementException("Product not found with id: " + id);
        }
        productRepo.deleteById(id);
        long end = System.currentTimeMillis();
        System.out.println("get all product: " + "Query time: " + (end - start) + "ms");
    }

    public ProductDTO editamount(ObjectId id, int amount) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
        product.setAmount(amount);
        return new ProductDTO(productRepo.save(product));
    }

    public ProductDTO updateProduct(ObjectId id, ProductUpdateRequest product) {
        long start = System.currentTimeMillis();
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

        long end = System.currentTimeMillis();
        System.out.println("update process: " + "Query time: " + (end - start) + "ms");
        long start1 = System.currentTimeMillis();
        Product saved = productRepo.save(existingProduct);
        long end1 = System.currentTimeMillis();
        System.out.println("db process: " + "Query time: " + (end1 - start1) + "ms");
        ProductDTO result = new ProductDTO(saved);
        long end2 = System.currentTimeMillis();
        System.out.println("db process: " + "Query time: " + (end2 - start) + "ms");
        return result;
    }

}
