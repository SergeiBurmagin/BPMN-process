package ru.starfish24.mascotte.dto.reservation;

import lombok.Data;

import java.util.List;

@Data
public class CancelReservationRequest {

    private String orderId;

    private List<ProductRequest> skuList;
}
