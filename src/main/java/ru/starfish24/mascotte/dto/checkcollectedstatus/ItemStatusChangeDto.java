package ru.starfish24.mascotte.dto.checkcollectedstatus;

import lombok.Data;

@Data
public class ItemStatusChangeDto {

    private Long orderItemId;

    private String oldStatus;

    private String newStatus;
}