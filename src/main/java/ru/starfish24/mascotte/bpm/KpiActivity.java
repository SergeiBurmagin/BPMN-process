package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.employeekpivalue.EmployeeKpiValueDto;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.employeekpivalue.EmployeeKpiService;
import ru.starfish24.mascotte.service.employeekpivalue.EmployeeKpiValueService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.EmployeeKpi;
import ru.starfish24.starfish24model.EmployeeKpiType;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Objects;

@Service
@Slf4j
public class KpiActivity implements JavaDelegate {

    private final EmployeeKpiValueService employeeKpiValueService;

    private final EmployeeKpiService employeeKpiService;

    private final ProductOrderRepository productOrderRepository;

    @Autowired
    public KpiActivity(EmployeeKpiValueService employeeKpiValueService, EmployeeKpiService employeeKpiService, ProductOrderRepository productOrderRepository) {
        this.employeeKpiValueService = employeeKpiValueService;
        this.employeeKpiService = employeeKpiService;
        this.productOrderRepository = productOrderRepository;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Long orderId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        String kpiCode = delegateExecution.getVariable(ProcessVar.KPI_CODE).toString();

        Objects.requireNonNull(kpiCode, "kpiCode is null");
        String variableName = delegateExecution.getVariable(ProcessVar.VARIABLE_NAME) != null ?
                delegateExecution.getVariable(ProcessVar.VARIABLE_NAME).toString() : null;

        String processVar = getProcessVariableByName(variableName, delegateExecution);

        Long employeeId = delegateExecution.getVariable(ProcessVar.EMPLOYEE_ID) != null ?
                Long.parseLong(delegateExecution.getVariable(ProcessVar.EMPLOYEE_ID).toString()) : null;
        try {
            EmployeeKpiType kpiType = getEmployeeKpiType(kpiCode, accountId);

            long recordValueTime = productOrder.getTimeCreated().getTime() - System.currentTimeMillis();

            EmployeeKpiValueDto employeeKpiValueDto = new EmployeeKpiValueDto();
            employeeKpiValueDto.setEmployeeId(employeeId);

            switch (kpiType) {
                case string:
                    employeeKpiValueDto.setRecordValueString(processVar);
                    break;
                case quantity:
                    employeeKpiValueDto.setRecordValueQuantity(Integer.parseInt(processVar));
                    break;
                case time:
                    employeeKpiValueDto.setRecordValueTime(recordValueTime);
            }
            employeeKpiValueDto.setKpiType(kpiType);
            employeeKpiValueDto.setAccountId(accountId);
            employeeKpiValueDto.setOrderId(orderId);
            employeeKpiValueDto.setCode(kpiCode);

            employeeKpiValueService.create(employeeKpiValueDto);
        } catch (RuntimeException runExc) {
            log.error(runExc.getMessage(), runExc);
        }
    }

    private String getProcessVariableByName(String variableName, DelegateExecution delegateExecution) {
        return delegateExecution.getVariable(variableName) != null ?
                delegateExecution.getVariable(variableName).toString() : null;
    }

    private EmployeeKpiType getEmployeeKpiType(String kpiCode, Long accountId) {
        if (kpiCode != null) {
            EmployeeKpi employeeKpi = employeeKpiService.getByCodeAndAccountId(kpiCode, accountId);
            if (employeeKpi == null)
                throw new RuntimeException(
                        String.format("Cant find kpi with code %s and account id %s, please set it up", kpiCode, accountId)
                );
            return employeeKpi.getKpiType();
        }
        return null;
    }
}
