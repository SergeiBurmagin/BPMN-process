package ru.starfish24.mascotte.bpm.orderuiupdate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.orderuiupdate.RefreshNotificationDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Arrays;
import java.util.Objects;

@Service
@Slf4j
public class RefreshNotificationActivity implements JavaDelegate {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    private HttpClient httpClient;

    @Value("${starfish24.ui.order.update.url}")
    private String urlUI;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, "Cant get orderId stop process");
        String event = execution.getVariable(ProcessVar.EVENT).toString();
        if (event != null && !event.isEmpty()) {
            switch (event) {
                case "ORDER_CANCELED":
                    // Создадим сигнал ORDER_CANCELED-${orderId}
                    runtimeService.createSignalEvent("ORDER_CANCELED-" + orderId)
                            .send();
                    break;
            }
        }

        RefreshNotificationDto dto = new RefreshNotificationDto();
        dto.setOrderIds(Arrays.asList(orderId));
        httpClient.postRequest(urlUI, dto);
    }


}
