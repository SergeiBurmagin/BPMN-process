package ru.starfish24.mascotte.bpm.orderconfirmation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StartOrderConfirmationListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        log.info("start CC01 process by order [orderId = {}]", delegateExecution.getVariable("orderId"));
        delegateExecution.setVariable("callCounter", 0);
    }
}
