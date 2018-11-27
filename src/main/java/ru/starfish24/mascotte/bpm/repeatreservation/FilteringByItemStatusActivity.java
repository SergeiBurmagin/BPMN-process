package ru.starfish24.mascotte.bpm.repeatreservation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.ProductOrderItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Класс- фильтрует записи по ItemOrderStatus = { defective, notreserved, notavailable }
 */
@Service
@Slf4j
public class FilteringByItemStatusActivity implements JavaDelegate {

    private final static String DEFECTIVE = "defective";
    private final static String NOT_RESERVED = "notreserved";
    private final static String NO_AVAILABLE = "noavailable";

    private ProductOrderRepository productOrderRepository;

    @Autowired
    public FilteringByItemStatusActivity(ProductOrderRepository productOrderRepository) {
        this.productOrderRepository = productOrderRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable("orderId").toString());
        // Сформируем карту которую пустим дальше по процесу
        Map<Long, byte[]> productOrderMap = new HashMap<>();
        try {
            Objects.requireNonNull(orderId, "Process variable orderId is not set, exit");
            ProductOrder productOrder = productOrderRepository.getOne(orderId);
            Objects.requireNonNull(productOrder, "Product order repository return null");
            List<ProductOrderItem> productOrderItemList = productOrder.getProductOrderItems();
            // найдем записи по искомым статусам
            List<ProductOrderItem> findItemList = productOrderItemList.stream()
                    .filter(this::findCondition).collect(Collectors.toList());
            findItemList.forEach(it -> productOrderMap.put(it.getId(), SerializationUtils.serialize(it)));
        } catch (NullPointerException npe) {
            log.error("FilteringByItemStatusActivity# get error = {}", npe);
        }
        log.info("productOrderMap {}", productOrderMap);
        execution.setVariable("productOrderMap", productOrderMap);
    }

    private boolean findCondition(ProductOrderItem item) {
        if (item != null && item.getProductOrderItemStatus() != null) {
            String code = item.getProductOrderItemStatus().getCode();
            log.debug("filtering code = {}", code);
            return Objects.equals(code, DEFECTIVE)
                    || Objects.equals(code, NO_AVAILABLE)
                    || Objects.equals(code, NOT_RESERVED);
        }
        return false;
    }
}
