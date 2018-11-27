package ru.starfish24.mascotte.service.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.employee.EmployeeRepository;
import ru.starfish24.starfish24model.Employee;
import ru.starfish24.starfish24model.EmployeeGroup;

import java.util.Objects;

@Service
@Slf4j
public class CamundaTaskServiceImpl implements CamundaTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public boolean checkTaskIdentity(String taskId, String login) {
        Employee employee = employeeRepository.findByLogin(login);
        boolean taskIdentity = isIdentityTaskForEmployee(taskId, employee);
        log.info("check task[id = {}] Identity login = '{}' : {}", taskId, login, taskIdentity);
        return taskIdentity;
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Employee findByLogin(String login) {
        return employeeRepository.findByLogin(login);
    }

    private boolean isIdentityTaskForEmployee(String taskId, Employee employee) {
        for (IdentityLink identityLink : taskService.getIdentityLinksForTask(taskId)) {
            if (identityLink == null) continue;

            String userId = identityLink.getUserId();
            if (userId != null) {
                if (Objects.equals(Objects.toString(employee.getId()), userId)) {
                    return true;
                }
            }
            String groupId = identityLink.getGroupId();
            if (groupId != null) {
                for (EmployeeGroup employeeGroup : employee.getEmployeeGroups()) {
                    if (Objects.equals(employeeGroup.getCode(), groupId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
