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
public class AllCollectedListener implements ExecutionListener {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        Objects.requireNonNull(productOrder, String.format("Cant get order with orderId = %s", orderId));
        boolean allCollected = productOrder.getProductOrderItems().stream()
                .filter(i -> !i.getDeleted() && i.getProductOrderItemStatus() != null)
                .allMatch(
                        i -> Objects.equals("pickedinwarehouse", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("pickedinsalon", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("readyforrelease", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("salon", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("shippedtowarehouse", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("receivedatwarehouse", i.getProductOrderItemStatus().getCode()) || Objects
                                .equals("tk", i.getProductOrderItemStatus().getCode()));

        execution.setVariable("allCollected", allCollected);
        log.debug("Start process Collect order with vars = {}", execution.getVariables());
        log.info("check all collected for orderId = {} [allCollected = {}]", orderId, allCollected);
    }
}
