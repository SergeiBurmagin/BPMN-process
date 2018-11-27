package ru.starfish24.mascotte.bpm.ordercollect;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Objects;

@Service
@Slf4j
public class AllWaitingListener implements ExecutionListener {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        boolean hasProblem = productOrder.getProductOrderItems().stream().filter(i -> !i.getDeleted()).anyMatch(
                i -> i.getProductOrderItemStatus() == null || Objects.equals("notreserved", i.getProductOrderItemStatus().getCode()) || Objects
                        .equals("noavailable", i.getProductOrderItemStatus().getCode()) || Objects
                        .equals("defective", i.getProductOrderItemStatus().getCode()));
        execution.setVariable("hasProblem", hasProblem);
        log.info("check any problem for orderId = {} [hasProblem = {}]", orderId, hasProblem);
    }
}
