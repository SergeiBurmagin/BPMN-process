package ru.starfish24.mascotte.bpm.cancelreservation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CalculateBuildTimeReport implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

    }
}
