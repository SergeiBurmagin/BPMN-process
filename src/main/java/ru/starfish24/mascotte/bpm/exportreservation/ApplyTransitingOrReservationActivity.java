package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Выполнить резервацию или перемещение
 */
@Service
@Slf4j
public class ApplyTransitingOrReservationActivity implements JavaDelegate {

    private final ReservationService reservationService;

    private final InternalReservationControllerApi internalReservationControllerApi;

    @Autowired
    public ApplyTransitingOrReservationActivity(ReservationService reservationService, InternalReservationControllerApi internalReservationControllerApi) {
        this.reservationService = reservationService;
        this.internalReservationControllerApi = internalReservationControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = new Long(delegateExecution.getVariable("orderId").toString());
        log.info("ApplyTransitingOrReservationActivity# Start reservation or transfer for orderId={}", orderId);
        List<InternalReservationDto> doReservation = new ArrayList<>();
        List<InternalReservationDto> doTransfer = new ArrayList<>();

        List<InternalReservationDto> internalReservationDtoList
                = internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        log.debug("ApplyTransitingOrReservationActivity# getting from starfish-service = {}", internalReservationDtoList);

        List<InternalReservationDto> _doReservation = internalReservationDtoList.stream().filter(
                x -> InternalReservationDto.StatusEnum.DO_RESERVE.equals(x.getStatus())
        ).collect(Collectors.toList());

        // Разберём данные и создадим список резервирования и список трансферов
        _doReservation.forEach(item -> {
            if (item.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE)) {
                // Трансфер
                if (item.getTransferStatus().equals(InternalReservationDto.TransferStatusEnum.TRANSFER)) {
                    doTransfer.add(item);
                } else {
                    doReservation.add(item);
                }
            }
        });

        if (log.isDebugEnabled()) {
            if (doTransfer.size() > 0)
                log.debug("ApplyTransitingOrReservationActivity# do transfer = {}",
                        doTransfer.stream().map(x -> x.getOrderItemId() + " qty " + x.getQuantity() +
                                " has be transfer  {from:" + x.getTransferWarehouseFrom().getName() + ", " +
                                "to:" + x.getTransferWarehouseTo().getName() + " }").collect(Collectors.joining(",")));
            if (doReservation.size() > 0)
                log.debug("ApplyTransitingOrReservationActivity# do reserve = {}", doReservation.stream().map(x -> x.getOrderItemId() + " qty " + x.getQuantity() +
                        " has be reserved {" + x.getReserveWarehouse().getName() + " }").collect(Collectors.joining(",")));
        }

        try {
            reservationService.doTransferByInternalReservationDtoList(orderId, doTransfer);
            reservationService.doReservationByInternalReservationDtoList(orderId, doReservation);
        } catch (Exception e) {
            log.error("Fail to execute transfer or reservation", e);
        }
        log.info("ApplyTransitingOrReservationActivity# done successfully");
    }
}
