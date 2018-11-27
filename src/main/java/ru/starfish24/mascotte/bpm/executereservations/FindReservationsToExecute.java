package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ChangeReservationActivityService;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Находит все операции по резервам котоыре нужно выполнить, и формирует из ник коллекцию
 * Т.К. в бп в качестве перменных нельзя использоваь пользовательские типы о которых не знает UI, делается преобразование данных в json строку
 */
@Slf4j
@Service
public class FindReservationsToExecute implements JavaDelegate {

    private final ChangeReservationActivityService changeReservationActivityService;
    private final ExecuteReservationService executeReservationService;

    @Autowired
    public FindReservationsToExecute(ChangeReservationActivityService changeReservationActivityService, ExecuteReservationService executeReservationService) {
        this.changeReservationActivityService = changeReservationActivityService;
        this.executeReservationService = executeReservationService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = null;
        try {
            orderId = VariableUtils.getLongOrThrow(ProcessVar.ORDER_ID, delegateExecution);
        } catch (Exception ex) {
            Integer val = (Integer) delegateExecution.getVariable(ProcessVar.ORDER_ID);
            orderId = new Long(val);
        }
        if (orderId == null) {
            throw new IllegalArgumentException("No order id specified");
        }

        List<InternalReservationDto> reservations = changeReservationActivityService.getInternalReservations(orderId);
        log.debug("changeReservationActivityService# Internal reservation = {}", reservations);
        Map<Long, String> toExecute = reservations.stream()
                .filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE)
                        || r.getStatus().equals(InternalReservationDto.StatusEnum.CANCEL_RESERVE)
                        || r.getStatus().equals(InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE))
                .collect(Collectors.toMap(r -> r.getId(), r -> executeReservationService.toString(r)));

        log.debug("changeReservationActivityService# toExecute created = {}", toExecute);
        delegateExecution.setVariable("toExecute", toExecute);
    }

}
