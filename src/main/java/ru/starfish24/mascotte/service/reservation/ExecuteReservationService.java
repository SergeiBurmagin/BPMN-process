package ru.starfish24.mascotte.service.reservation;

import io.swagger.client.model.InternalReservationDto;

public interface ExecuteReservationService {

    String toString(InternalReservationDto dto);

    InternalReservationDto toDto(String dto);
}
