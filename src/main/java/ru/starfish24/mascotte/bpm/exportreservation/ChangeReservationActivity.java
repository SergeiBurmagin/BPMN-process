package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.OrderControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.ItemDto;
import io.swagger.client.model.ProductOrderDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ChangeReservationActivityService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChangeReservationActivity implements JavaDelegate {


    private final ChangeReservationActivityService changeReservationActivityService;
    private final OrderControllerApi orderControllerApi;

    public ChangeReservationActivity(ChangeReservationActivityService changeReservationActivityService, OrderControllerApi orderControllerApi) {
        this.changeReservationActivityService = changeReservationActivityService;
        this.orderControllerApi = orderControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        ProductOrderDto dto = orderControllerApi.getOrderUsingGET(orderId);
        final String externalOrderId = dto.getId();
        List<InternalReservationDto> reservations = changeReservationActivityService.getInternalReservations(orderId);
        log.debug("Count of internal reservation fro order = {} count = {}", externalOrderId, reservations.size());
        Map<Long, List<InternalReservationDto>> reserved = changeReservationActivityService.findExistReservations(reservations);

        if (log.isDebugEnabled()) {
            log.debug("Order id={} items are: {}", externalOrderId, dto.getItems().stream().map(ItemDto::toString).collect(Collectors.joining(",\n")));
        }

        //Если заказ омтенен то его колличество надо установить в ноль.
        nullifyQuantityMarkedToDeleteItem(dto, externalOrderId);

        //Нужно найти те позиции которые  не совпадают по колличеству.
        Map<Long, Double> diff = changeReservationActivityService.findDiff(reserved, dto.getItems());
        if (log.isDebugEnabled()) {
            log.debug("Diff for order=[{}] is: \n{}", externalOrderId, diff.entrySet().stream()
                    .map(e -> "item=" + e.getKey() + " diff=" + e.getValue())
                    .collect(Collectors.joining("\n")));
        }


        Map<Long, Double> toReserve = diff.entrySet().stream().filter(p -> p.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Long, Double> toCancelReserve = diff.entrySet().stream().filter(p -> p.getValue() < 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<InternalReservationDto> manuals = findManualReserves(orderId);
        log.debug("Get manuals = {}", manuals);

        if (manuals != null && manuals.size() > 0) {
            for (InternalReservationDto manual : manuals) {
                toCancelReserve.put(manual.getProductId(), manual.getQuantity());
            }
        }

        if (log.isDebugEnabled()) {

            log.debug("toReserve for order ={} is {}", externalOrderId, toReserve.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",")));
            log.debug("toCancelReserve for order ={} is {}", externalOrderId, toCancelReserve.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",")));
        }

        delegateExecution.setVariable("toReserve", toReserve);
        delegateExecution.setVariable("toCancelReserve", toCancelReserve);
    }

    private Double getQuantity(Long productId, List<ItemDto> items) {
        for (ItemDto item : items) {
            if (item.getId().equals(productId))
                return item.getQuantity().doubleValue();
        }
        return 0d;
    }


    private List<InternalReservationDto> findManualReserves(Long orderId) {
        return changeReservationActivityService.getInternalReservations(orderId)
                .stream().filter(i -> i.getStatus().equals(InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE))
                .collect(Collectors.toList());
    }

    private void nullifyQuantityMarkedToDeleteItem(ProductOrderDto dto, String externalOrderId) {
        dto.getItems().stream().forEach(x -> {
            if (x.isDeleted()) {
                log.debug("Order item for order =[{}] with id=[{}] name=[{}] was deleted and reservation will be canceled", externalOrderId, x.getId(), x.getName());
                x.setQuantity(0);
            }
        });
    }


}
