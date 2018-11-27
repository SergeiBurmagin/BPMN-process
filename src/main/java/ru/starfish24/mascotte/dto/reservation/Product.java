package ru.starfish24.mascotte.dto.reservation;

import lombok.Data;

import java.io.Serializable;

@Data
public class Product implements Serializable {

    private Long productId;

    private Integer quantity;

    public Product() {
    }

    public Product(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Product(Long id, Double quantity) {
        this(id, quantity.intValue());
    }
}
