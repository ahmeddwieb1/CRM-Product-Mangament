package org.elmorshedy.product.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepo extends MongoRepository<Product, ObjectId> {
    List<Product> findAllBy(Pageable pageable);
}
