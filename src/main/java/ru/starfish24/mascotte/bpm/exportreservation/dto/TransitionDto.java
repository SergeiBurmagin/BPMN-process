package ru.starfish24.mascotte.bpm.exportreservation.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Перемщение со склада на склад.
 */
@Data
public class TransitionDto implements Serializable {

    private WarehouseDto from;

    private WarehouseDto to;

}
