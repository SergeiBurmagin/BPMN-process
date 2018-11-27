package ru.starfish24.mascotte.dto.reservation;

import lombok.Data;

import java.util.List;

@Data
public class ReservationRequest extends CancelReservationRequest {

    private String orderGUID;

    private String warehouseId;

    private String targetWarehouseId;

    private String transitWarehouseId;

}
