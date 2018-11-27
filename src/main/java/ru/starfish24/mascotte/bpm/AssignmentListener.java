package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.AssignmentDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
public class AssignmentListener implements TaskListener {

    private final HttpClient httpClient;

    @Value("${starfish24.ui.assignment.task.url}")
    private String urlUI;

    @Autowired
    public AssignmentListener(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Object externalId = delegateTask.getVariable(ProcessVar.ORDER_EXTERNAL_ID);
        Long orderId = Long.parseLong(delegateTask.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, "OrderId is null");
        String id = delegateTask.getId();
        String description = delegateTask.getDescription();
        String name = delegateTask.getName();
        Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, c -> {
            AssignmentDto assignmentDto = new AssignmentDto();
            assignmentDto.setId(id);
            assignmentDto.setTaskName(name);
            assignmentDto.setDescription(description);

            try {
                httpClient.postRequest(urlUI, assignmentDto);
            } catch (IOException e) {
                log.error("cannot sent event", e);
            }
            log.info("create user task [taskId = {}, taskName = {}, description = {}, externalId = {}]", id, name, description, externalId);
        });
    }
}
