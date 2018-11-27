package ru.starfish24.mascotte.bpm.checkcollectedstatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.checkcollectedstatus.ItemStatusChangeDto;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CheckCollectedStatusActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private RuntimeService runtimeService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        List<ItemStatusChangeDto> statusMap = MAPPER.readValue(execution.getVariable(ProcessVar.ITEM_STATUS_MAP).toString(), TypeFactory
                .defaultInstance().constructCollectionType(List.class, ItemStatusChangeDto.class));
        boolean changeFromWaiting = statusMap.stream().anyMatch(s -> Objects.equals("waiting", s.getOldStatus()));
        boolean hasWaitingStatus = productOrder.getProductOrderItems().stream().anyMatch(i -> i.getProductOrderItemStatus() != null && Objects.equals("waiting", i.getProductOrderItemStatus().getCode()) && !i.getDeleted());

        log.info("check collected status for orderId = {} [changeFromWaiting = {}, hasWaitingStatus = {}]", orderId, changeFromWaiting, hasWaitingStatus);
        if (changeFromWaiting && !hasWaitingStatus) {
            String signal = String.format("ORDER_COLLECTED-%s", orderId);
            runtimeService.createSignalEvent(signal).send();
            log.info("signal {} created", signal);
        }
    }
}
