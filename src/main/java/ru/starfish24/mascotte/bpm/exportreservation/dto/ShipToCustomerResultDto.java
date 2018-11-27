package ru.starfish24.mascotte.bpm.exportreservation.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO которое формируется в результате выполнеия БП ShippingToCustomer
 */
@Data
public class ShipToCustomerResultDto implements Serializable {

    private OrderItemDto productOrderItem;

    private WarehouseDto reserve;

    private WarehouseDto target;

    private WarehouseDto transit;

    private TransitionDto transition;

}
