package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.dto.reservation.Product;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExecuteTransfer implements JavaDelegate {

    private final ExecuteReservationService executeReservationService;

    private final ReservationService reservationService;

    public ExecuteTransfer(ExecuteReservationService executeReservationService, ReservationService reservationService) {
        this.executeReservationService = executeReservationService;
        this.reservationService = reservationService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = Long.valueOf(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        Map<Long, String> toExecute = (Map<Long, String>) delegateExecution.getVariable("toExecute");
        List<InternalReservationDto> dtos = toExecute.entrySet().stream().map(r -> executeReservationService.toDto(r.getValue())).collect(Collectors.toList());
        dtos.stream().filter(r -> r.getStatus() != null)
                .filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE))
                .filter(r -> r.getTransferStatus() != null)
                .filter(r -> InternalReservationDto.TransferStatusEnum.TRANSFER.equals(r.getTransferStatus()))
                .collect(Collectors.groupingBy(InternalReservationDto::getTransferWarehouseFrom))
                .entrySet().forEach(fromGroup -> fromGroup.getValue().stream().collect(Collectors.groupingBy(InternalReservationDto::getTransferWarehouseTo))
                .entrySet().forEach(toGroup -> {
                    List<Product> products = reservationService.getProducts(toGroup.getValue());
                    IntegrationServiceResponse response = reservationService.createTransfer(orderId, products, toGroup.getKey().getExternalId(), fromGroup.getKey().getExternalId());
                    if (response.getSuccess()) {
                        toGroup.getValue().stream().forEach(r -> {
                            r = reservationService.confirmInternalTransfer(r);
                            toExecute.put(r.getId(), executeReservationService.toString(r));
                        });

                    } else {
                        log.debug(String.format("Failed to do transfer [orderId = %s]", orderId));
                        toGroup.getValue().stream().forEach(r -> {
                            try {
                                toExecute.put(r.getId(), executeReservationService.toString(reservationService.internalReservationFailed(r)));
                            } catch (RuntimeException re) {
                                log.error("cant convert object to json string {} ", re.getCause());
                            }
                        });
                    }
                })
        );

    }
}
