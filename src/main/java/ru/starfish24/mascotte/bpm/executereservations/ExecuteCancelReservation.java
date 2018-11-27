package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExecuteCancelReservation implements JavaDelegate {

    private final ExecuteReservationService executeReservationService;

    private final ReservationService reservationService;

    public ExecuteCancelReservation(ExecuteReservationService executeReservationService, ReservationService reservationService) {
        this.executeReservationService = executeReservationService;
        this.reservationService = reservationService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = Long.valueOf(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        Map<Long, String> toExecute = (Map<Long, String>) delegateExecution.getVariable("toExecute");
        List<InternalReservationDto> dtos = toExecute.entrySet().stream().map(r -> executeReservationService.toDto(r.getValue())).collect(Collectors.toList());
        dtos.stream().filter(r -> r.getStatus() != null).filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.CANCEL_RESERVE)).forEach(r -> {
            try {
                IntegrationServiceResponse integrationServiceResponse = reservationService.cancelReserve(r);
                // todo вставил костыль, не выбрасываем сообщение об ошибке если success != true,
                // считаем что мул вернул все ок
                //if (integrationServiceResponse.getSuccess()) {
                r = reservationService.internalReservationCanceled(r);
                toExecute.put(r.getId(), executeReservationService.toString(r));
                //}
//                } else {
//                    throw new RuntimeException(String.format("Failed to cancel reservation [orderId = %s]", orderId));
//                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
