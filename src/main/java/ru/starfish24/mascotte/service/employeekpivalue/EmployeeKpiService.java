package ru.starfish24.mascotte.service.employeekpivalue;

import ru.starfish24.starfish24model.EmployeeKpi;

public interface EmployeeKpiService {

    EmployeeKpi getByCodeAndAccountId(String code, Long accountId);

}
