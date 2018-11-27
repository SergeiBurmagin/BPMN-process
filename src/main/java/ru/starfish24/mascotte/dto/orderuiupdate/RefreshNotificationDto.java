package ru.starfish24.mascotte.dto.orderuiupdate;

import lombok.Data;

import java.util.List;

@Data
public class RefreshNotificationDto {

    private List<Long> orderIds;
}
