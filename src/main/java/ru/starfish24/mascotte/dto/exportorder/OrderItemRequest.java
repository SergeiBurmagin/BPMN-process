package ru.starfish24.mascotte.dto.exportorder;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemRequest {

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal fixedWeight;

    private BigDecimal estimatedWeight;

    private String name;

    private String productId;

    private BigDecimal discount;

    private List<CustomAttributeRequest> customAttributes;
}
