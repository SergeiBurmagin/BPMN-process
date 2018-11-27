package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.starfish24.mascotte.kafka.producer.ProcessEventProducer;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.status.StatusRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Status;
import ru.starfish24.starfish24model.constant.EventCode;

import java.util.*;

@Service
@Slf4j
@Transactional
public class ChangeOrderStatusActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ProcessEventProducer processEventProducer;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());
        Long accountId = Long.parseLong(execution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        String statusCode = execution.getVariableLocal("statusCode").toString();

        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        List<Status> statuses = statusRepository.findByAccount(accountId);
        Optional<Status> newStatusOpt = statuses.stream().filter(s -> Objects.equals(s.getCode(), statusCode)).findFirst();
        if (!newStatusOpt.isPresent()) {
            log.error("status [code = {}, orderId = {}] not found", statusCode, orderId);
            return;
        }
        newStatusOpt.ifPresent(productOrder::setCurrentStatus);

        Map<String, Object> processParams = new HashMap<>();
        processParams.put(ProcessVar.ORDER_ID, orderId);
        processParams.put(ProcessVar.ACCOUNT_ID, accountId);
        processParams.put(ProcessVar.EVENT, EventCode.ORDER_STATUS_CHANGED.name());

        processEventProducer.sendMessage(processParams);
        log.info("product order status changed [orderId = {}, status = {}]", orderId, statusCode);
    }

}
