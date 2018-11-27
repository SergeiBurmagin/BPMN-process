package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetupCancelReservationActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;

    private final ReservationService reservationService;

    @Autowired
    public SetupCancelReservationActivity(InternalReservationControllerApi internalReservationControllerApi, ReservationService reservationService) {
        this.internalReservationControllerApi = internalReservationControllerApi;
        this.reservationService = reservationService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        log.debug("Starting SetupCancelReservationActivity for order = [{}]", orderId);
        Map<Long, Double> toCancel = (Map<Long, Double>) delegateExecution.getVariable("toCancelReserve");
        if (log.isDebugEnabled()) {
            log.debug("toCancel Reserve = {}", toCancel.entrySet().stream().map(p -> p.getKey() + "=" + p.getValue()).collect(Collectors.joining(",")));
        }
        //Получить список сделанных резервов
        List<InternalReservationDto> internalReservationDtoList = internalReservationControllerApi.findByOrderIdUsingGET(orderId);

        List<InternalReservationDto> manuals = internalReservationDtoList.stream().filter(
                i -> i.getStatus().equals(InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE)
        ).collect(Collectors.toList());

        internalReservationDtoList.stream().filter(item -> findManuals(item.getNextReservation(), manuals))
                .collect(Collectors.toList()).forEach(item -> {
            if (item.getStatus().equals(InternalReservationDto.StatusEnum.RESERVED)) {
                log.debug("Cancel quantity = {}  for reservation = {}", item.getQuantity(), item);
                InternalReservationDto created = reservationService.createInternalReservationDto(InternalReservationRequest.StatusEnum.CANCEL_RESERVE,
                        item.getQuantity(),
                        item.getOrderItemId(),
                        item.getTargetWarehouse().getId(),
                        item.getReserveWarehouse().getId(),
                        item.getTransitWarehouse() == null ? null : item.getTransitWarehouse().getId(),
                        item.getTransferWarehouseFrom() == null ? null : item.getTransferWarehouseFrom().getId(),
                        item.getTransferWarehouseTo() == null ? null : item.getTransferWarehouseTo().getId());
                log.debug("New reservation manual reservation operation created = {}", created);
//
//                item.setStatus(InternalReservationDto.StatusEnum.CANCEL_RESERVE);
//                internalReservationControllerApi.updateUsingPOST(item);
            }
        });

        log.debug("Start change reservation quantity for reservations with status DO_RESERVE");
        internalReservationDtoList.stream()
                .filter(i -> i.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE)
                        && i.getNextReservation() == null
                        && toCancel.containsKey(i.getProductId())).forEach(i -> {
            //если есть остаток по резерву, то просто меняем колличетво.
            if (toCancel.get(i.getProductId()) > 0) {
                log.debug("change quantity of reservation = {}", i);
                i.setQuantity(toCancel.get(i.getProductId()));
            } else { //если надо устновить в ноль, то отменяем резерв польностью
                log.debug("canceling reservation = {}", i);
                i.setStatus(InternalReservationDto.StatusEnum.RESERVE_CANCELED);
            }
            //Сохранить данные о резерве
            internalReservationControllerApi.updateUsingPOST(i);
            log.debug("reservation successfully updated ={}", i);
            //задание по меньшению колличество или отмене резерва выполненно.
            toCancel.remove(i.getProductId());
        });

        log.debug("Start change reservation quantity for reservations with status RESERVED");
        internalReservationDtoList.stream().filter(i ->
                i.getStatus().equals(InternalReservationDto.StatusEnum.RESERVED)
                        && i.getNextReservation() == null
                        && toCancel.containsKey(i.getProductId()))
                .forEach(i -> {
                    // Если заказ не обнуляется
                    Double diff = i.getQuantity() + toCancel.get(i.getProductId());
                    if (diff > 0) {
                        //то делаем отмену резерва на указнное колличество
                        //TODO: вынести эту логику в сервисы, т.к. тут мы должны просто пользоваться предоставленным функционалом, а не определять правила отмены резервирования.
                        log.debug("change quantity for reseravation that is allready compleated = ", i.toString());
                        double toCancelQuantity = i.getQuantity() - Math.abs(toCancel.get(i.getProductId()));
                        log.debug("Cancel quantity = {}  for reservation = {}", toCancelQuantity, i);
                        InternalReservationDto created = reservationService.createInternalReservationDto(InternalReservationRequest.StatusEnum.CANCEL_RESERVE,
                                toCancelQuantity,
                                i.getOrderItemId(),
                                i.getTargetWarehouse().getId(),
                                i.getReserveWarehouse().getId(),
                                i.getTransitWarehouse() == null ? null : i.getTransitWarehouse().getId(),
                                i.getTransferWarehouseFrom() == null ? null : i.getTransferWarehouseFrom().getId(),
                                i.getTransferWarehouseTo() == null ? null : i.getTransferWarehouseTo().getId());
                        log.debug("New reservation for cancel reservation operation created = {}", created);

                    } else {
                        //Если заказ полностью отменяется
                        //То просто отменяем заказ.
                        log.debug("Execute full reservation cancel for = {}", i);
                        internalReservationControllerApi.cancelUsingPOST(i.getId());
                    }
                    toCancel.remove(i.getProductId());
                });
    }

    private boolean findManuals(InternalReservationDto nextReservation, List<InternalReservationDto> manuals) {
        if (nextReservation != null && manuals != null) {
            for (InternalReservationDto manual : manuals) {
                if (manual.getId().equals(nextReservation.getId()))
                    return true;
            }
        }
        return false;
    }
}
