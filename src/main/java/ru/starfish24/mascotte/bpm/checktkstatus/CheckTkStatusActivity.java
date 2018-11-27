package ru.starfish24.mascotte.bpm.checktkstatus;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.ProcessSignalService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Objects;

@Service
@Slf4j
public class CheckTkStatusActivity implements JavaDelegate {

    private final ProductOrderRepository productOrderRepository;

    private final ProcessSignalService processSignalService;

    @Autowired
    public CheckTkStatusActivity(ProductOrderRepository productOrderRepository, ProcessSignalService processSignalService) {
        this.productOrderRepository = productOrderRepository;
        this.processSignalService = processSignalService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);

        boolean allInTkStatus = productOrder.getProductOrderItems().stream()
                .filter(i -> !i.getDeleted())
                .allMatch(i -> i.getProductOrderItemStatus() != null &&
                        Objects.equals("tk", i.getProductOrderItemStatus().getCode())
                );
        if (allInTkStatus) {
            processSignalService.sendSignal("ORDER_DROPPED_TO_DELIVERY", orderId);
            processSignalService.sendSignal("ORDER_COLLECTED", orderId);
        }
    }
}
