package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.api.WarehouseControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.WarehouseDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcessFailedTransferActivityTest {

    @Mock
    private InternalReservationControllerApi internalReservationControllerApi;

    @Mock
    private ReservationService reservationService;

    @Mock
    private WarehouseControllerApi warehouseControllerApi;

    @Mock
    DelegateExecution delegateExecution;

    ProcessFailedTransferActivity failedTransferActivity;

    @Before
    public void setup() {
        failedTransferActivity = new ProcessFailedTransferActivity(internalReservationControllerApi, reservationService, warehouseControllerApi);
    }

    @Test
    public void execute() throws Exception {
        List<Long> failedReservers = new ArrayList<>();
        long rId = 1L;
        failedReservers.add(rId);
        failedReservers.add(rId);
        InternalReservationDto internalReservation = new InternalReservationDto();
        Long productId = 45L;
        internalReservation.setProductId(productId);
        WarehouseDto reserveWarehouse = new WarehouseDto();
        Long warehouseId = 33L;
        reserveWarehouse.setId(warehouseId);
        internalReservation.setReserveWarehouse(reserveWarehouse);
        Double quantity = 10.;
        internalReservation.setQuantity(quantity);
        when(internalReservationControllerApi.getUsingGET(rId)).thenReturn(internalReservation);
        when(warehouseControllerApi.removeFromStockUsingPOST(productId, quantity, warehouseId)).thenReturn(10.0);
        when(delegateExecution.getVariable("failedToTransfer")).thenReturn(failedReservers);
        failedTransferActivity.execute(delegateExecution);
        verify(warehouseControllerApi, times(2)).removeFromStockUsingPOST(productId, quantity, warehouseId);
    }
}