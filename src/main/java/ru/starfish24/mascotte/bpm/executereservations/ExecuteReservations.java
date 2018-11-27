package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseExtId;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.dto.reservation.Product;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.service.shipping.ShippingService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Выполнить резервы
 */
@Service
@Slf4j
public class ExecuteReservations implements JavaDelegate {

    private final ExecuteReservationService executeReservationService;

    private final ReservationService reservationService;

    final ShippingService shippingService;

    @Autowired
    public ExecuteReservations(ExecuteReservationService executeReservationService, ReservationService reservationService, ShippingService shippingService) {
        this.executeReservationService = executeReservationService;
        this.reservationService = reservationService;
        this.shippingService = shippingService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        List<Product> products = new ArrayList<>();
        final Long orderId = Long.valueOf(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        Map<Long, String> toExecute = (Map<Long, String>) delegateExecution.getVariable("toExecute");
        List<InternalReservationDto> dtos = toExecute.entrySet().stream().map(r -> executeReservationService.toDto(r.getValue())).collect(Collectors.toList());
        log.info("internal reservations: {}", dtos);
        dtos.stream().filter(r -> r.getStatus() != null).filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE))
                .collect(Collectors.groupingBy(s -> new WarehouseExtId(s.getReserveWarehouse() != null ? s.getReserveWarehouse().getExternalId() : null, s.getTargetWarehouse() != null ? s.getTargetWarehouse().getExternalId() : null, s.getTransitWarehouse() != null ? s.getTransitWarehouse().getExternalId() : null)))
                .entrySet().forEach(entry -> {
            List<Product> res = reservationService.getProducts(entry.getValue());
            if (!products.contains(res)) {
                products.addAll(res);
                String warehouseExternalId = entry.getKey().getReserve();
                String transitWarehouseExternalId = entry.getKey().getTransit();
                String targetWarehouseExternalId = entry.getKey().getTarget();
                IntegrationServiceResponse response = reservationService.createReservation(
                        orderId,
                        reservationService.getProducts(entry.getValue()),
                        warehouseExternalId, transitWarehouseExternalId, targetWarehouseExternalId);
                log.info("reservation created: {}", reservationService.getProducts(entry.getValue()));
                // todo надо доработать, если запрос прошел и success, а если нет придумать что делаем дальше
                //if (response.getSuccess()) {

                    entry.getValue().stream().forEach(r -> {
                        r = reservationService.confirmInternalReservation(r, "Reservation is created");
                        toExecute.put(r.getId(), executeReservationService.toString(r));
                    });
//                } else {

//                    throw new RuntimeException(String.format("Failed to do reserves [orderId = %s]", orderId));
//                }
            }
        });
    }

}
