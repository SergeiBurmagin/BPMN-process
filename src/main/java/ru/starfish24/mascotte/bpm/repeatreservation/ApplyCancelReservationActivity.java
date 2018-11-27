package ru.starfish24.mascotte.bpm.repeatreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import ru.starfish24.mascotte.repository.productorderitem.ProductOrderItemRepository;
import ru.starfish24.starfish24model.ProductOrderItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Отправим "отмену резерва" если productOrderMap не пуста
 */
@Service
@Slf4j
public class ApplyCancelReservationActivity implements JavaDelegate {

    private InternalReservationControllerApi internalReservationControllerApi;
    private final ProductOrderItemRepository productOrderItemRepository;

    @Autowired
    public ApplyCancelReservationActivity(InternalReservationControllerApi internalReservationControllerApi,
                                          ProductOrderItemRepository productOrderItemRepository) {
        this.internalReservationControllerApi = internalReservationControllerApi;
        this.productOrderItemRepository = productOrderItemRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(execution.getVariable("orderId").toString());
        Map<Long, byte[]> productOrderMap =
                (Map<Long, byte[]>) execution.getVariable("productOrderMap");
        log.debug("{}", productOrderMap);
        // получим все резервы по заказу
        List<InternalReservationDto> reservationDtoList =
                internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        // нас интересуют только актуальные
        final List<InternalReservationDto> immutableList = reservationDtoList.stream()
                .filter(it -> it.getNextReservation() == null).collect(Collectors.toList());

        Map<Long, Double> toReserve = new HashMap<>();

        if (!productOrderMap.isEmpty()) {
            productOrderMap.forEach((k, v) -> {
                cancelReservation(immutableList, k);
                if (v.length > 0) {
                    ProductOrderItem deserialize = (ProductOrderItem) SerializationUtils.deserialize(v);
                    ProductOrderItem productOrderItem = productOrderItemRepository.getOne(deserialize.getId());
                    toReserve.put(productOrderItem.getProduct().getId(), productOrderItem.getQuantity().doubleValue());
                }
            });
        }
        log.info("toReserve = {}", toReserve);
        execution.setVariable("toReserve", toReserve);
    }

    /**
     * отправим запрос отмену резерва
     *
     * @param dtoList
     * @param productItemId
     */
    private void cancelReservation(List<InternalReservationDto> dtoList, Long productItemId) {
        if (dtoList != null) {
            dtoList.forEach(it -> {
                if (productItemId.equals(it.getOrderItemId()))
                    internalReservationControllerApi.cancelUsingPOST(it.getId());
            });
        }
    }
}
