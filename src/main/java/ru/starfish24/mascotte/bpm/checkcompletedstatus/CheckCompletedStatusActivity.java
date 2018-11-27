package ru.starfish24.mascotte.bpm.checkcompletedstatus;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Objects;

@Service
@Slf4j
public class CheckCompletedStatusActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);

        boolean hasAnyCompletedStatus = productOrder.getProductOrderItems().stream().filter(i -> !i.getDeleted()).anyMatch(i -> i.getProductOrderItemStatus() != null && Objects
                .equals("completed", i.getProductOrderItemStatus().getCode()));
        if (hasAnyCompletedStatus) {
            String signal = String.format("ORDER_RECEIVED-%s", orderId);
            runtimeService.createSignalEvent(signal).send();
            log.info("signal {} created", signal);
        }
    }
}
