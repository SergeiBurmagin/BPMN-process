package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * По информации из order-broker и резервам с проваленыым перемещеинем, формирует новый список  запросов на резервы.
 */
@Service
@Slf4j
public class SetupFailedTransferActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;

    private final ReservationService reservationService;

    @Autowired
    public SetupFailedTransferActivity(InternalReservationControllerApi internalReservationControllerApi, ReservationService reservationService) {
        this.internalReservationControllerApi = internalReservationControllerApi;
        this.reservationService = reservationService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        List<Long> failedToTransfer = (List<Long>) delegateExecution.getVariable("failedToTransfer");
        Long orderId = new Long(delegateExecution.getVariable("orderId").toString());
        log.info("Starting SetupFailedTransferActivity# for order = [{}]", orderId);
        List<InternalReservationDto> failedReservations = failedToTransfer.stream().map(l -> internalReservationControllerApi.getUsingGET(l)).collect(Collectors.toList());
        Set<Long> failedOrderItems = failedReservations.stream().map(r -> r.getOrderItemId()).collect(Collectors.toSet());
        List<ShipToCustomerResultDto> roadMap = (List<ShipToCustomerResultDto>) delegateExecution.getVariable(ProcessVar.RESERVATION_ROADMAP);
        //выбрать только те позиции которые не удалось перместить.
        roadMap = roadMap.stream().filter(r -> failedOrderItems.contains(r.getProductOrderItem().getId())).collect(Collectors.toList());
        failedReservations.stream().forEach(r -> reservationService.internalReservationCanceled(r));
        reservationService.createInternalReservations(roadMap);
    }
}
