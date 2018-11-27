package ru.starfish24.mascotte.bpm.checksalonstatus;

import lombok.extern.slf4j.Slf4j;
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
public class CheckSalonStatusActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);

        boolean hasAnySalonStatus = productOrder.getProductOrderItems().stream().filter(i -> !i.getDeleted()).anyMatch(i -> i.getProductOrderItemStatus() != null && Objects
                .equals("salon", i.getProductOrderItemStatus().getCode()));
        execution.setVariable("hasAnySalonStatus", hasAnySalonStatus);
        log.info("check any salon status for orderId = {} [hasAnySalonStatus = {}]", orderId, hasAnySalonStatus);
    }
}
