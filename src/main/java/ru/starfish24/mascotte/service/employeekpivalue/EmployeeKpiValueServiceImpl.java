package ru.starfish24.mascotte.service.employeekpivalue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.starfish24.mascotte.dto.employeekpivalue.EmployeeKpiValueDto;
import ru.starfish24.mascotte.repository.employee.EmployeeRepository;
import ru.starfish24.mascotte.repository.employeekpi.EmployeeKpiRepository;
import ru.starfish24.mascotte.repository.employeekpivalue.EmployeeKpiValueRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.starfish24model.*;

import java.util.List;

@Service
@Slf4j
public class EmployeeKpiValueServiceImpl implements EmployeeKpiValueService {

    private final EmployeeKpiRepository employeeKpiRepository;

    private final EmployeeKpiValueRepository employeeKpiValueRepository;

    private final ProductOrderRepository productOrderRepository;

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeKpiValueServiceImpl(EmployeeKpiRepository employeeKpiRepository, EmployeeKpiValueRepository employeeKpiValueRepository, ProductOrderRepository productOrderRepository, EmployeeRepository employeeRepository) {
        this.employeeKpiRepository = employeeKpiRepository;
        this.employeeKpiValueRepository = employeeKpiValueRepository;
        this.productOrderRepository = productOrderRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void create(EmployeeKpiValueDto employeeKpiValueDto) throws RuntimeException {

        EmployeeKpiType kpiType = employeeKpiValueDto.getKpiType();
        String code = employeeKpiValueDto.getCode();
        Long accountId = employeeKpiValueDto.getAccountId();

        EmployeeKpi employeeKpi = employeeKpiRepository.findByCodeAndAccount(code, accountId);
        if (employeeKpi == null)
            employeeKpi = createEmployeeKpi(kpiType, code, accountId);

        ProductOrder productOrder = getProductOrder(employeeKpiValueDto);

        EmployeeKpiValue employeeKpiValue = new EmployeeKpiValue();
        Employee employee = getEmployee(employeeKpiValueDto.getEmployeeId());

        if (employeeKpi.getMultiply() == null || !employeeKpi.getMultiply()) {
            // Обновление записи
            List<EmployeeKpiValue> employeeKpiValueList = employeeKpiValueRepository
                    .findByEmployeeAndProductOrderAndEmployeeKpi(employee, productOrder, employeeKpi);
            if (employeeKpiValueList != null) {
                // Больше одного значиния найдено, выведем сообщение о том что
                if (employeeKpiValueList.size() > 1) {
                    throw new RuntimeException(String.format(
                            "More than one Kpi Attribute found by [employee = %s, employeeKpi = %s, orderId = %s], " +
                                    "but multiply is false or null",
                            employee.getLogin(), employeeKpi.getCode(), productOrder.getExternalId()
                    ));
                } else {
                    if (!employeeKpiValueList.isEmpty())
                        employeeKpiValue = employeeKpiValueList.get(0);
                }
            }
            update(employeeKpiValueDto, employeeKpi, productOrder, employeeKpiValue, employee);

            log.info("EmployeeKpiValue updated [id = {}, kpiType = {}, time = {}, quantity = {}]",
                    employeeKpiValue.getId(), kpiType,
                    employeeKpiValue.getRecordValueTime(), employeeKpiValue.getRecordValueQuantity());
        } else {
            // создание новой
            update(employeeKpiValueDto, employeeKpi, productOrder, employeeKpiValue, employee);
            log.info("EmployeeKpiValue created [id = {}, kpiType = {}, time = {}, quantity = {}]",
                    employeeKpiValue.getId(), kpiType,
                    employeeKpiValue.getRecordValueTime(), employeeKpiValue.getRecordValueQuantity());
        }

    }

    private void update(EmployeeKpiValueDto employeeKpiValueDto, EmployeeKpi employeeKpi, ProductOrder productOrder, EmployeeKpiValue employeeKpiValue, Employee employee) {
        employeeKpiValue.setEmployee(employee);
        employeeKpiValue.setEmployeeKpi(employeeKpi);
        employeeKpiValue.setRecordTime(employeeKpiValueDto.getRecordTime());
        employeeKpiValue.setRecordValueTime(employeeKpiValueDto.getRecordValueTime());
        employeeKpiValue.setRecordValueQuantity(employeeKpiValueDto.getRecordValueQuantity());
        employeeKpiValue.setRecordValueStr(employeeKpiValueDto.getRecordValueString());
        employeeKpiValue.setProductOrder(productOrder);
        employeeKpiValueRepository.save(employeeKpiValue);
    }

    private EmployeeKpi createEmployeeKpi(EmployeeKpiType kpiType, String code, Long accountId) {
        EmployeeKpi employeeKpi = new EmployeeKpi();
        employeeKpi.setAccount(new Account().id(accountId));
        employeeKpi.setCode(code);
        employeeKpi.setName(code);
        employeeKpi.setKpiType(kpiType);
        employeeKpiRepository.save(employeeKpi);
        log.info("EmployeeKpi created [id = {}, code = '{}', kpiType = {}, accountId = {}]", employeeKpi.getId(),
                employeeKpi.getCode(),
                employeeKpi.getKpiType(),
                accountId
        );
        return employeeKpi;
    }

    private ProductOrder getProductOrder(EmployeeKpiValueDto employeeKpiValueDto) {
        Long orderId = employeeKpiValueDto.getOrderId();
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        if (productOrder == null) {
            throw new RuntimeException(String.format("ProductOrder [id = %s] not found", orderId));
        }
        return productOrder;
    }

    /**
     * Метод находит и возвращает Employee или null
     *
     * @param employeeId
     * @return
     */
    private Employee getEmployee(Long employeeId) {
        if (employeeId != null) {
            Employee employee = employeeRepository.findOne(employeeId);
            if (employee == null) {
                throw new RuntimeException(String.format("Employee [id = %s] not found", employeeId));
            }
            return employee;
        }
        return null;
    }
}
