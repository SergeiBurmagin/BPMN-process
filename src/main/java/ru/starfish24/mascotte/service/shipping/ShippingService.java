package ru.starfish24.mascotte.service.shipping;

import io.swagger.client.model.InternalReservationDto;

public interface ShippingService {

    void updateReservationInfoByOrderId(InternalReservationDto dto, Long orderId);

}
