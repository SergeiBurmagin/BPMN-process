package ru.starfish24.mascotte.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Objects;

@Service
@Slf4j
public class OrderIdCheckListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.debug("orderIdCheckListener = {}", execution.getVariables());
        Object orderId = execution.getVariable(ProcessVar.ORDER_ID);
        Objects.requireNonNull(orderId, String.format("Error, start processId with orderId = null [tenantId = %s, " +
                "process = %s]", execution.getTenantId(), execution.getProcessInstanceId()));
    }
}
