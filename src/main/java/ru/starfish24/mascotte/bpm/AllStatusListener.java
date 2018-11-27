package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.ProductOrderItem;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AllStatusListener implements TaskListener {

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Override
    public void notify(DelegateTask delegateTask) {
        Long orderId = Long.parseLong(delegateTask.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, "The process var orderId is not set, terminate");
        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        List<ProductOrderItem> items = productOrder.getProductOrderItems();
        // ZM-414 в рамках задачи сказано что товар будет один, значит будем брать первый
        Objects.requireNonNull(items,
                String.format("Something wrong with order = %s, where is no have any product items", orderId));

        ProductOrderItem item = items.stream().findFirst().orElse(null);
        Objects.requireNonNull(item, "ProductOrderItem list is empty");
        String item_status = item.getProductOrderItemStatus().getCode();
        delegateTask.setVariable("item_status", item_status);
    }
}
