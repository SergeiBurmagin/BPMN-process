package ru.starfish24.mascotte.bpm.ordercollect;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

@Service
public class StartOrderCollectListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("allCollected", false);
        delegateExecution.setVariable("additionalReserves", 0);
        delegateExecution.setVariable("rereserveSuccessful", false);
    }
}
