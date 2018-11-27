package ru.starfish24.mascotte.bpm.exportreservation.dto;

import io.swagger.client.model.WarehouseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarehousesMapping {
    private WarehouseDto reserve;
    private WarehouseDto target;
    private WarehouseDto transit;
}
