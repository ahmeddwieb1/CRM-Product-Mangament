package org.elmorshedy.product.model;

import lombok.Data;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private Double price;
    private Integer amount;
    private String description;

    public ProductDTO(Product product) {
        this.id = product.getId().toHexString();
        this.name = product.getName();
        this.price = product.getPrice();
        this.amount = product.getAmount();
        this.description = product.getDescription();
    }
}
