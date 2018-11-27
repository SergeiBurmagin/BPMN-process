package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.api.WarehouseControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.util.List;

/**
 * Резервы по которым неудалось выполнить пермещение, должны быть закрыты и на их основе должны  быть созданны резервы.
 * Так же нужно обнулить остатки на складах по которым не удалось выполнить перемещение.
 * Этот шаг нужен перед повторным вызовом брокера, что бы он вернул по товарам другие склады.
 */
@Service
@Slf4j
public class ProcessFailedTransferActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;
    private final ReservationService reservationService;
    private final WarehouseControllerApi warehouseControllerApi;

    public ProcessFailedTransferActivity(InternalReservationControllerApi internalReservationControllerApi, ReservationService reservationService, WarehouseControllerApi warehouseControllerApi) {
        this.internalReservationControllerApi = internalReservationControllerApi;
        this.reservationService = reservationService;
        this.warehouseControllerApi = warehouseControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        List<Long> failedToTransfer =
                (List<Long>) delegateExecution.getVariable("failedToTransfer");
        if (failedToTransfer != null && failedToTransfer.size() > 0) {
            failedToTransfer.forEach(id -> {
                try {
                    InternalReservationDto parent = internalReservationControllerApi.getUsingGET(id);
                    reservationService.internalReservationCanceled(parent);
                    warehouseControllerApi.removeFromStockUsingPOST(parent.getProductId(), parent.getQuantity(), parent.getReserveWarehouse().getId());
                } catch (HttpMessageNotReadableException ex) {
                    log.error("{}", ex);
                }
            });
        }
    }
}
