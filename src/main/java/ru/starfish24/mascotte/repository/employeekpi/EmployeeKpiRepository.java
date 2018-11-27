package ru.starfish24.mascotte.repository.employeekpi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.starfish24.starfish24model.EmployeeKpi;

public interface EmployeeKpiRepository extends JpaRepository<EmployeeKpi, Long> {

    EmployeeKpi findByCodeAndAccount(@Param("code") String code, @Param("accountId") Long accountId);
}
