package ru.starfish24.mascotte.bpm.usertasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.task.CamundaTaskService;
import ru.starfish24.mascotte.service.task.TaskUIRefreshService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.Utils;
import ru.starfish24.starfish24model.Employee;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FindNotAssignedTasks implements JavaDelegate {

    private final TaskService camundaTaskService;
    private final TaskUIRefreshService taskUIRefreshService;
    private final CamundaTaskService employeeTaskService;

    @Autowired
    public FindNotAssignedTasks(TaskService camundaTaskService, TaskUIRefreshService taskUIRefreshService, CamundaTaskService employeeTaskService) {
        this.camundaTaskService = camundaTaskService;
        this.taskUIRefreshService = taskUIRefreshService;
        this.employeeTaskService = employeeTaskService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long accountId = Long.parseLong(execution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        String userLogin = execution.getVariable(ProcessVar.USER_ID).toString();
        Employee employee = employeeTaskService.findByLogin(userLogin);

        Integer troubleTasks = Integer.parseInt(execution.getVariable("troubleTasks").toString());
        Integer confirmTasks = Integer.parseInt(execution.getVariable("confirmTasks").toString());
        Integer warehouseTasks = Integer.parseInt(execution.getVariable("warehouseTasks").toString());
        Integer performingTime = Integer.parseInt(execution.getVariable("performingTime").toString());

        Objects.requireNonNull(accountId, "Process var accountId is not set");
        Objects.requireNonNull(employee, "Cant find employee by user = " + userLogin);
        List<Task> callCustomerTasksToApply =
                assigneeTaskToUserByGroup(employee.getLogin(), confirmTasks, performingTime, "%callCustomer%");

        // Позвонить на склад/в магазин
        List<Task> pushWarehousesTasks = assigneeTaskToUserByGroup(employee.getLogin(), warehouseTasks,
                performingTime, "pushWarehouseTask");

        // Обработка задач в статусе "Проблема"
        List<Task> troubleTasksToApply =
                assigneeTaskToUserByGroup(employee.getLogin(), troubleTasks, performingTime, "%orderTrouble%");

        if (log.isDebugEnabled()) {
            log.debug("Assign to userLogin = {}, accountId = {}, confirmTasks = {}, troubleTasks = {} ", userLogin, accountId,
                    callCustomerTasksToApply.stream().map(Object::toString)
                            .collect(Collectors.joining(",")),
                    troubleTasksToApply.stream().map(Object::toString)
                            .collect(Collectors.joining(","))
            );
        }
        taskUIRefreshService.refreshSignal();
        log.info("To user with id = {} assignee {} tasks", userLogin,
                callCustomerTasksToApply.size() + troubleTasksToApply.size());
    }

    private List<Task> assigneeTaskToUserByGroup(String userLogin, Integer countAssigneeTasks,
                                                 Integer performingTime, String groupId) {
        List<Task> userConfirmTasks = camundaTaskService.createTaskQuery()
                .taskDefinitionKeyLike(groupId)
                .taskUnassigned()
                .orderByTaskCreateTime()
                .asc()
                .list();
        log.debug("selected task by Group = {} is {}", groupId, userConfirmTasks.size());
        // отфильтруемся
        List<Task> taskListToApply = filterByAssigneeOrDueDate(userConfirmTasks, countAssigneeTasks);
        // Назначим задачи
        applyAssignee(taskListToApply, userLogin, performingTime);
        return taskListToApply;
    }


    private List<Task> filterByAssigneeOrDueDate(List<Task> sourceList, Integer countToAssignee) {
        if (sourceList != null)
            return sourceList.stream()
                    .filter(this::filterCondition)
                    .limit(countToAssignee)
                    .collect(Collectors.toList());
        return null;
    }

    /**
     * Условия выборки
     *
     * @param task user task
     * @return true если выполненые все условия выборки
     */
    private boolean filterCondition(Task task) {
        log.debug("filtring {}", task.toString());
        LocalDateTime now = LocalDateTime.now();
        final boolean overDue = (task.getDueDate() != null) &&
                Utils.convertDateToLocalDateTime(task.getDueDate()).isBefore(now);
        final boolean isAssignee = (task.getAssignee() == null || task.getAssignee().isEmpty());

        return isAssignee || overDue;
    }

    /**
     * Назначим задачи пользователю
     *
     * @param sourceList
     * @param userLogin      логин пользователя
     * @param performingTime время на обработку задачи
     */
    private void applyAssignee(List<Task> sourceList, String userLogin, Integer performingTime) {

        if (sourceList != null) {
            sourceList.forEach(i -> {
                Date date = Utils.convertLocalDateTimeToDate(LocalDateTime.now().plusMinutes(
                        new Long(performingTime)
                ));
                i.setAssignee(userLogin);
                i.setDueDate(date);
                log.debug("assignee task {} to {} due date {}", i.getTaskDefinitionKey(),
                        i.getAssignee(), i.getDueDate());
                camundaTaskService.saveTask(i);
            });
        }
    }
}
