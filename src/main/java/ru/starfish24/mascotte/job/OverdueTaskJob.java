package ru.starfish24.mascotte.job;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.starfish24.mascotte.service.task.TaskUIRefreshService;
import ru.starfish24.mascotte.utils.Utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OverdueTaskJob {
    /**
     * Внутренний сервис камунды
     */
    final TaskService taskService;
    /**
     * Сервис взаимодействия с UI
     */
    final TaskUIRefreshService taskUIRefreshService;

    @Autowired
    public OverdueTaskJob(TaskService taskService, TaskUIRefreshService taskUIRefreshService) {
        this.taskService = taskService;
        this.taskUIRefreshService = taskUIRefreshService;
    }

    /**
     * Метод снимает с пользователя просроченные задачи
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void unAssigneeUserTasks() {
        // получим список тасков удовлетворяющих нашему условию
        List<Task> overdue = taskService.createTaskQuery()
                .list().stream()
                .filter(this::findOverdueCondition)
                .collect(Collectors.toList());
        if (overdue != null && !overdue.isEmpty()) {
            overdue.forEach(it -> {
                it.setAssignee(null);
                it.setDueDate(null);
                taskService.saveTask(it);
            });
        }
        taskUIRefreshService.refreshSignal();
    }

    private boolean findOverdueCondition(Task task) {
        if (task != null) {
            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                LocalDateTime dueDate = Utils.convertDateToLocalDateTime(task.getDueDate());
                if (dueDate != null && dueDate.isBefore(LocalDateTime.now()))
                    return true;
            }
        }
        return false;
    }
}
