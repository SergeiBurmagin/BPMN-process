package ru.starfish24.mascotte.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса для генирации сигналов камунды
 */
@Service
@Slf4j
public class ProcessSignalServiceImpl implements ProcessSignalService {

    private RuntimeService runtimeService;

    @Autowired
    public ProcessSignalServiceImpl(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public void sendSignal(String signalName, Long orderId) {
        final String signal = String.format("%s-%s", signalName, orderId);
        runtimeService.createSignalEvent(signal)
                .send();
        log.info("Singnal {} created", signal);
    }
}
