package ru.starfish24.mascotte.bpm.exportreservation;

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

@Service
@Slf4j
public class ReservationActivity implements JavaDelegate {

    @Autowired
    private ReservationService reservationService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        List<ShipToCustomerResultDto> reservations = (List<ShipToCustomerResultDto>) delegateExecution.getVariable(ProcessVar.RESERVATION_ROADMAP);
        Long orderId = new Long(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        //Формирование списка резервов и пермещеинй которые нужно сделать по заказа.
        List<InternalReservationDto> internalReservationDtos = reservationService.createInternalReservations(reservations);

        try {
            reservationService.doTransferByInternalReservationDtoList(orderId, internalReservationDtos);
            reservationService.doReservationByInternalReservationDtoList(orderId, internalReservationDtos);
        } catch (Exception e) {
            log.error("Failed to execute reservations or transfer", e);
        }

    }
}
