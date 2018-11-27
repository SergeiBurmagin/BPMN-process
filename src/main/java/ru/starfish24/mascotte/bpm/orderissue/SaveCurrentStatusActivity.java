package ru.starfish24.mascotte.bpm.orderissue;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;

@Service
@Slf4j
public class SaveCurrentStatusActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        execution.setVariable("tempOrderStatus", productOrder.getCurrentStatus().getCode());
        log.info("save current status [orderExtId = {}, status = {}]", productOrder.getExternalId(), productOrder.getCurrentStatus().getCode());
    }
}
