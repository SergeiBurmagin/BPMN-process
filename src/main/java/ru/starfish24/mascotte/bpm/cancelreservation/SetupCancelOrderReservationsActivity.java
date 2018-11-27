package ru.starfish24.mascotte.bpm.cancelreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Для указанного заказа устонавливает все резервы в статус Отменить.
 * Анализируем текущее состояние резервов и для тех по которым нужно совершить отмену, выставляем статус "отменить".
 */
@Service
@Slf4j
public class SetupCancelOrderReservationsActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;

    @Autowired
    public SetupCancelOrderReservationsActivity(InternalReservationControllerApi internalReservationControllerApi) {
        this.internalReservationControllerApi = internalReservationControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        List<InternalReservationDto> reservationDtoList = internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        reservationDtoList.stream().filter(r -> r.getStatus() != null)
                .filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE)).forEach(r ->
                internalReservationControllerApi.canceledUsingPOST(r.getId()));
        reservationDtoList.stream().filter(r -> r.getStatus() != null)
                .filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.RESERVED)).forEach(r -> internalReservationControllerApi.cancelUsingPOST(r.getId()));
    }
}
