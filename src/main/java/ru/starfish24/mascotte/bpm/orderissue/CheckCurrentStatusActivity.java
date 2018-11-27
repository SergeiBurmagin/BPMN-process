package ru.starfish24.mascotte.bpm.orderissue;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.ProductOrder;

@Service
@Slf4j
public class CheckCurrentStatusActivity implements ExecutionListener {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Long orderId = new Long(VariableUtils.getIntOrThrow(ProcessVar.ORDER_ID, execution));
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        execution.setVariable("currentOrderStatus", productOrder.getCurrentStatus().getCode());
        log.info("check current status [orderExtId = {}, status = {}]", productOrder.getExternalId(), productOrder.getCurrentStatus().getCode());
    }
}
