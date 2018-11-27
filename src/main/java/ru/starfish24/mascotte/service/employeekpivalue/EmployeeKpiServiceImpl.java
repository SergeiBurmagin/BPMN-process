package ru.starfish24.mascotte.service.employeekpivalue;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.employeekpi.EmployeeKpiRepository;
import ru.starfish24.starfish24model.EmployeeKpi;

@Service
@Slf4j
public class EmployeeKpiServiceImpl implements EmployeeKpiService {

    final EmployeeKpiRepository employeeKpiRepository;

    @Autowired
    public EmployeeKpiServiceImpl(EmployeeKpiRepository employeeKpiRepository) {
        this.employeeKpiRepository = employeeKpiRepository;
    }

    @Override
    public EmployeeKpi getByCodeAndAccountId(String code, Long accountId) {
        return employeeKpiRepository.findByCodeAndAccount(code, accountId);
    }
}
