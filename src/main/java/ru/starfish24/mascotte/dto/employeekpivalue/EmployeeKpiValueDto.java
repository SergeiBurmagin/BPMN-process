package ru.starfish24.mascotte.dto.employeekpivalue;

import lombok.Data;
import ru.starfish24.starfish24model.EmployeeKpiType;

import java.util.Date;

@Data
public class EmployeeKpiValueDto {

    private EmployeeKpiType kpiType;
    private String code;
    private Long orderId;
    private Long employeeId;
    private Long accountId;
    private Long recordValueTime;
    private Integer recordValueQuantity;
    private String recordValueString;
    private Date recordTime = new Date();
}
