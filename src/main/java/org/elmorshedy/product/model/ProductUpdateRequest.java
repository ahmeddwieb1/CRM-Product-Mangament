package org.elmorshedy.product.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateRequest {
    @Size(max = 100, message = "Name too long")
    private String name;
    
    @Min(0)
    private Integer amount;
    
    @DecimalMin("0.0")
    private Double price;
    
    @Size(max = 500)
    private String description;

    // Getters and Setters
}