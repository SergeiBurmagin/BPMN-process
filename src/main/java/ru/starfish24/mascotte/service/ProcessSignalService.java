package ru.starfish24.mascotte.service;

/**
 * Сервис для генирации сигналов камунды
 */
public interface ProcessSignalService {
    void sendSignal(String signalName, Long orderId);
}
