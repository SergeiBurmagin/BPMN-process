package ru.starfish24.mascotte.bpm.orderdelivery;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Objects;

@Service
@Slf4j
public class AllCompletedListener implements ExecutionListener {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Long orderId = new Long(VariableUtils.getIntOrThrow(ProcessVar.ORDER_ID, execution));
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        boolean allCompleted = productOrder.getProductOrderItems().stream().filter(i -> !i.getDeleted()).allMatch(i -> Objects
                .equals("completed", i.getProductOrderItemStatus().getCode()));
        execution.setVariable("allCompleted", allCompleted);
        log.info("check all completed [allCompleted = {}]", allCompleted);
    }
}
