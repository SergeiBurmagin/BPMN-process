package ru.starfish24.mascotte.bpm.exportreservation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ReservationService;

@Service
@Slf4j
public class AddReservationActivity implements JavaDelegate {

    private final ReservationService registerableService;

    public AddReservationActivity(ReservationService registerableService) {
        this.registerableService = registerableService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        log.info("Mock add reservations to order = [{}]", orderId);
    }
}
