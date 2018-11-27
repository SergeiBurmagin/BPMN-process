package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CancelReservationActivity implements JavaDelegate {

    private final ReservationService reservationService;

    private final InternalReservationControllerApi internalReservationControllerApi;

    @Autowired
    public CancelReservationActivity(ReservationService reservationService, InternalReservationControllerApi internalReservationControllerApi) {
        this.reservationService = reservationService;
        this.internalReservationControllerApi = internalReservationControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = (Long) delegateExecution.getVariable("orderId");
        log.debug("Start reservation cancel for orderId={}", orderId);
        List<InternalReservationDto> cancelTransfer = new ArrayList<>();
        Set<InternalReservationDto> failedCancel = new HashSet<>();
        List<InternalReservationDto> internalReservationDtos = internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        List<InternalReservationDto> toCancel = internalReservationDtos.stream()
                .filter(i -> i.getStatus().equals(InternalReservationDto.StatusEnum.CANCEL_RESERVE) && i.getNextReservation() == null)
                .collect(Collectors.toList());
        if (toCancel != null) {
            toCancel.stream().filter(c -> c != null).forEach(c -> {
                InternalReservationDto parent = internalReservationDtos.stream().filter(f -> f.getNextReservation() != null
                        && f.getNextReservation().getId().equals(c.getId())).findFirst().orElse(null);
                if (c.getTransferWarehouseFrom() != null) {
                    boolean transferWasDone = parent != null
                            && InternalReservationDto.TransferStatusEnum.DONE.equals(parent.getTransferStatus())
                            && InternalReservationDto.TransferStatusEnum.CANCELED.equals(c.getTransferStatus());
                    if (transferWasDone) {
                        cancelTransfer.add(c);
                    }
                }
            });
        }
        log.debug("{} tranfers will be canceled for orderId={}", cancelTransfer.size(), orderId);

        //Выполнить отмену перемещения
        cancelTransfer.stream().forEach(t -> {
            try {
                //TODO: тут может быть ошибка, например: отмена резерва через шину удалась, но информацию об этом сохранить не смогли. Нужно наверное переделывать БП на событийную модель.
                IntegrationServiceResponse response = reservationService.cancelTransfer(t);
                if (response.getSuccess()) {
                    log.debug("confirm transfer canceling");
                    reservationService.cancelInternalTransfer(t);
                } else {
                    log.error("Failed to cancel transfer with message = [{}] and result= [{}] for {}", response.getMessage(), response.getResult(), t);
                }
            } catch (Exception ex) {
                log.error("Failed to cancel transfer for {}", t);
                failedCancel.add(t);
            }
        });

        //Выполнить отмену резервов
        log.debug("Start sending cancel reservation to integrationService");
        log.debug("{} reservation will be canceled for orderId={}", toCancel.size(), orderId);
        toCancel.stream().forEach(c -> {
            if (!failedCancel.contains(c)) {
                try {
                    IntegrationServiceResponse response = reservationService.cancelReserve(c);
                    if (response.getSuccess()) {
                        reservationService.internalReservationCanceled(c);
                    } else {
                        log.error("Failed to cancel reservation with message = [{}] and result= [{}] for {}", response.getMessage(), response.getResult(), c);
                    }
                } catch (Exception ex) {
                    log.error("Failed to cancel reservation for {}", c);
                }
            } else {
                log.info("Cancel reservation was not performed, because transfer was not canceled. Reservation={}", c);
            }
        });

    }
}
