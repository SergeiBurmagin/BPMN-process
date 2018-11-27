package ru.starfish24.mascotte.service.task;

import ru.starfish24.starfish24model.Employee;

public interface CamundaTaskService {

    boolean checkTaskIdentity(String taskId, String login);

    Employee getEmployeeById(Long id);

    Employee findByLogin(String login);
}
