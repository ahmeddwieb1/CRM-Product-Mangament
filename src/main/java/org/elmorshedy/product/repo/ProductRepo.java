package org.elmorshedy.product.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepo extends MongoRepository<Product, ObjectId> {
}
