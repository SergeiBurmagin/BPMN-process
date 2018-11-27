package ru.starfish24.mascotte.repository.employeekpivalue;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.starfish24.starfish24model.Employee;
import ru.starfish24.starfish24model.EmployeeKpi;
import ru.starfish24.starfish24model.EmployeeKpiValue;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.List;

public interface EmployeeKpiValueRepository extends JpaRepository<EmployeeKpiValue, Long> {
    List<EmployeeKpiValue> findByEmployeeAndProductOrderAndEmployeeKpi(Employee employee, ProductOrder productOrder, EmployeeKpi employeeKpi);
}
