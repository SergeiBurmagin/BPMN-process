package ru.starfish24.mascotte.bpm.orderuicreate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.orderuiupdate.RefreshNotificationDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Arrays;

@Service
@Slf4j
public class CreateNotificationActivity implements JavaDelegate {

    @Autowired
    private HttpClient httpClient;

    @Value("${starfish24.ui.order.create.url}")
    private String urlUI;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        RefreshNotificationDto dto = new RefreshNotificationDto();
        dto.setOrderIds(Arrays.asList(orderId));
        httpClient.postRequest(urlUI, dto);
    }
}

