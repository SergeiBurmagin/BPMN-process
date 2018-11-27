package ru.starfish24.mascotte.bpm.orderchange;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
public class RecreateReservationActivity implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

    }
}
