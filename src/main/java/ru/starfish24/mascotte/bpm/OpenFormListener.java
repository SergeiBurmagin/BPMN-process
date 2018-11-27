package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.OpenFormDto;
import ru.starfish24.mascotte.http.HttpClient;

import java.io.IOException;

@Service
@Slf4j
public class OpenFormListener implements TaskListener {

    @Autowired
    private HttpClient httpClient;

    @Value("${starfish24.ui.open.form.url}")
    private String urlUI;

    @Override
    public void notify(DelegateTask delegateTask) {
        OpenFormDto dto = new OpenFormDto();
        dto.setAssignee(delegateTask.getAssignee());
        dto.setTaskId(delegateTask.getId());
        dto.setFormKey(delegateTask.getBpmnModelElementInstance().getCamundaFormKey());
        dto.setProcessInstanceId(delegateTask.getProcessInstanceId());

        Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, c -> {
            try {
                httpClient.postRequest(urlUI, dto);
            } catch (IOException e) {
                log.error("failed to OpenForm send {} to {}", dto, urlUI);
            }
            log.info("send message {} to {}", dto, urlUI);
        });
    }
}
