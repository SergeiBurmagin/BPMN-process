package ru.starfish24.mascotte.bpm.orderchange;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.kafka.producer.ProcessEventProducer;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.HashMap;
import java.util.Map;

@Service
public class UiRefreshListener implements ExecutionListener {

    @Autowired
    private ProcessEventProducer processEventProducer;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put(ProcessVar.ORDER_ID, execution.getVariable(ProcessVar.ORDER_ID));
        message.put(ProcessVar.ACCOUNT_ID, execution.getVariable(ProcessVar.ACCOUNT_ID));
        message.put(ProcessVar.EVENT, "UI_REFRESH");

        processEventProducer.sendMessage(message);
    }
}
