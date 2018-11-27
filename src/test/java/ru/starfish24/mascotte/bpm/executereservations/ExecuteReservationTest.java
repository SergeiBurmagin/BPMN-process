package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.WarehouseDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteReservationTest {

    public static final long TO_CANCEL_ID = 1L;
    public static final long TO_RESERVE_ID = 2L;
    public static final long ORDER_ID = 100L;
    public static final long TO_TRANSFER_ID = 3L;

    @Mock
    private ExecuteReservationService executeReservationService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private DelegateExecution delegateExecution;

    private ExecuteCancelReservation executeCancelReservation;

    private ExecuteTransfer executeTransfer;

    private Map<Long, String> toExecute;
    private InternalReservationDto toCancel;
    private InternalReservationDto toReserve;
    private IntegrationServiceResponse successResponse;
    private WarehouseDto fromWarehouse;
    private WarehouseDto toWarehouse;

    @Before
    public void setup() throws IOException {
        executeCancelReservation = new ExecuteCancelReservation(executeReservationService, reservationService);
        executeTransfer = new ExecuteTransfer(executeReservationService, reservationService);
        toExecute = new HashMap<>();
        toExecute.put(TO_CANCEL_ID, String.valueOf(TO_CANCEL_ID));
        toExecute.put(TO_RESERVE_ID, String.valueOf(TO_RESERVE_ID));
        toCancel = new InternalReservationDto();
        toCancel.setStatus(InternalReservationDto.StatusEnum.CANCEL_RESERVE);
        toCancel.setId(TO_CANCEL_ID);
        fromWarehouse = new WarehouseDto();
        fromWarehouse.setId(101L);
        toWarehouse = new WarehouseDto();
        toWarehouse.setId(102L);
        toCancel.setTransferWarehouseFrom(fromWarehouse);

        toCancel.setTransferWarehouseTo(toWarehouse);
        toReserve = new InternalReservationDto();
        toReserve.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);
        toReserve.setId(TO_RESERVE_ID);
        when(executeReservationService.toDto(String.valueOf(TO_CANCEL_ID))).thenReturn(toCancel);
        when(executeReservationService.toDto(String.valueOf(TO_RESERVE_ID))).thenReturn(toReserve);
        when(delegateExecution.getVariable("toExecute")).thenReturn(toExecute);
        when(delegateExecution.getVariable("orderId")).thenReturn(ORDER_ID);


        successResponse = new IntegrationServiceResponse();
        successResponse.setSuccess(true);
        when(reservationService.cancelReserve(toCancel)).thenReturn(successResponse);
        when(reservationService.internalReservationCanceled(any())).thenAnswer(args -> args.getArguments()[0]);

    }

    @Test
    public void cancelReservationExecute() throws Exception {
        when(executeReservationService.toString(toCancel)).thenReturn("testValue");
        executeCancelReservation.execute(delegateExecution);
        assertEquals("testValue", toExecute.get(TO_CANCEL_ID));
    }

    @Test
    public void doTransfer() throws Exception {
        InternalReservationDto toTransfer = new InternalReservationDto();
        toTransfer.setId(TO_TRANSFER_ID);
        toTransfer.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);
        toTransfer.setTransferStatus(InternalReservationDto.TransferStatusEnum.TRANSFER);
        toTransfer.setTransferWarehouseTo(toWarehouse);
        toTransfer.setTransferWarehouseFrom(fromWarehouse);
        toExecute.put(TO_TRANSFER_ID, String.valueOf(TO_TRANSFER_ID));
        when(executeReservationService.toString(toTransfer)).thenReturn("testValue");
        when(executeReservationService.toDto(String.valueOf(TO_TRANSFER_ID))).thenReturn(toTransfer);
        when(reservationService.getProducts(any())).thenReturn(new ArrayList<>());
        when(reservationService.createTransfer(eq(ORDER_ID), any(), any(), any())).thenReturn(successResponse);
        when(reservationService.confirmInternalTransfer(toTransfer)).thenAnswer(args -> args.getArguments()[0]);
        executeTransfer.execute(delegateExecution);
        assertEquals("testValue", toExecute.get(TO_TRANSFER_ID));
    }

}