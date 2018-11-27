package ru.starfish24.mascotte.bpm.exportreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarehouseExtId {

    private String reserve;
    private String target;
    private String transit;
}
