package ru.starfish24.mascotte.bpm.exportreservation.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderItemDto implements Serializable {

    private Long id;
    private Long productId;
    private String productExternalId;
    private String productName;
    private int quantity;
}
