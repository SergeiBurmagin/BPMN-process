package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.exportreservation.dto.OrderItemDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.TransitionDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.dto.reservation.Product;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationActivityTest {

    @Mock
    private DelegateExecution delegateExecution;

    @InjectMocks
    private ReservationActivity reservationActivity;

    @Captor
    private ArgumentCaptor<List<Product>> listArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<Product>> transferCaptor;

    @Mock
    private ReservationService reservationService;

    @Test
    public void execute() throws Exception {
        List<ShipToCustomerResultDto> result = new ArrayList<>();
        Integer startQuantity = 10;
        long start = new Long(startQuantity + 100);
        result.add(createShipDto("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId1", startQuantity), true));
        result.add(createShipDto("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId2", startQuantity + 1), true));
        result.add(createShipDto("wtarget", "wreserve", null, createOrderItemDto("itemExternalId3", startQuantity + 2), false));
        result.add(createShipDto("wtarget", "wreserve", null, createOrderItemDto("itemExternalId4", startQuantity + 3), false));
        result.add(createShipDto("wtarget1", "wreserve", null, createOrderItemDto("itemExternalId5", startQuantity + 4), false));
        result.add(createShipDto("wtarget1", "wreserve", null, createOrderItemDto("itemExternalId6", startQuantity + 5), false));
        when(delegateExecution.getVariable(ProcessVar.RESERVATION_ROADMAP)).thenReturn(result);
        Integer orderId = 1;
        Long longOrderId = 1L;
        when(delegateExecution.getVariable(ProcessVar.ORDER_ID)).thenReturn(orderId);

        List<InternalReservationDto> reservations = new ArrayList<>();
        reservations.add(createReservation("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId1", startQuantity), true));
        reservations.add(createReservation("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId2", startQuantity + 1), true));
        reservations.add(createReservation("wtarget", "wreserve", null, createOrderItemDto("itemExternalId3", startQuantity + 2), false));
        reservations.add(createReservation("wtarget", "wreserve", null, createOrderItemDto("itemExternalId4", startQuantity + 3), false));
        reservations.add(createReservation("wtarget1", "wreserve", null, createOrderItemDto("itemExternalId5", startQuantity + 4), false));
        reservations.add(createReservation("wtarget1", "wreserve", null, createOrderItemDto("itemExternalId6", startQuantity + 5), false));

        when(reservationService.createInternalReservations(result)).thenReturn(reservations);

        IntegrationServiceResponse response = new IntegrationServiceResponse();
        response.setSuccess(true);
        when(reservationService.createTransfer(any(), any(), any(), any())).thenReturn(response);
        IntegrationServiceResponse successResponse = new IntegrationServiceResponse();
        successResponse.setSuccess(true);
        when(reservationService.createReservation(eq(longOrderId), anyListOf(Product.class), anyString(), anyString(), anyString())).thenReturn(successResponse);
        when(reservationService.confirmInternalTransfer(any())).then(args -> {
            InternalReservationDto dto = (InternalReservationDto) args.getArguments()[0];
            dto.setTransferStatus(InternalReservationDto.TransferStatusEnum.DONE);
            return dto;
        });

        // todo
        reservationActivity.execute(delegateExecution);
//        verify(reservationService, times(3)).createReservation(eq(longOrderId), listArgumentCaptor.capture(), anyString(), anyString(), anyString());
//        verify(reservationService, times(1)).createTransfer(eq(longOrderId), transferCaptor.capture(), any(), any());
//
//        assertEquals(2, transferCaptor.getValue().size());
//        List<Product> products = listArgumentCaptor.getAllValues().get(2);
//
//        assertEquals(startQuantity, products.get(0).getQuantity());
//        assertEquals(110L, (long) products.get(0).getProductId());
//        assertEquals(start + 1, (long) products.get(1).getProductId());
//
//        products = listArgumentCaptor.getAllValues().get(0);
//        assertEquals(start + 2, (long) products.get(0).getProductId());
//        assertEquals(start + 3, (long) products.get(1).getProductId());
//
//        products = listArgumentCaptor.getAllValues().get(1);
//        assertEquals(start + 4, (long) products.get(0).getProductId());
//        assertEquals(start + 5, (long) products.get(1).getProductId());
    }


    @Test
    public void executeExcludeTest() throws Exception {
        List<ShipToCustomerResultDto> result = new ArrayList<>();
        Integer startQuantity = 10;
        result.add(createShipDto("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId1", startQuantity), true));
        result.add(createShipDto("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId2", startQuantity + 1), true));
        when(delegateExecution.getVariable(ProcessVar.RESERVATION_ROADMAP)).thenReturn(result);

        List<InternalReservationDto> reservations = new ArrayList<>();
        reservations.add(createReservation("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId1", startQuantity), true));
        reservations.add(createReservation("wtarget", "wreserve", "wtransit", createOrderItemDto("itemExternalId2", startQuantity + 1), true));
        Integer orderId = 1;
        Long longOrderId = 1L;
        when(delegateExecution.getVariable(ProcessVar.ORDER_ID)).thenReturn(orderId);

        when(reservationService.createInternalReservations(result)).thenReturn(reservations);
        InternalReservationDto noTransferReservation = new InternalReservationDto();
        noTransferReservation.setTransferStatus(InternalReservationDto.TransferStatusEnum.CANCELED);
        when(reservationService.cancelInternalTransfer(any())).then(invoke -> noTransferReservation);
        IntegrationServiceResponse response = new IntegrationServiceResponse();
        response.setSuccess(false);
        when(reservationService.createTransfer(eq(longOrderId), any(), any(), any())).thenReturn(response);
        when(reservationService.createReservation(eq(longOrderId), anyListOf(Product.class), anyString(), anyString(), anyString())).thenReturn(new IntegrationServiceResponse());
        reservationActivity.execute(delegateExecution);
        verify(reservationService, never()).createReservation(eq(longOrderId), listArgumentCaptor.capture(), anyString(), anyString(), anyString());
    }


    private OrderItemDto createOrderItemDto(String externalId, int quantity) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setProductExternalId(externalId);
        orderItemDto.setQuantity(quantity);
        orderItemDto.setId(Long.valueOf(quantity));
        orderItemDto.setProductId(100L + quantity);
        return orderItemDto;
    }

    private ShipToCustomerResultDto createShipDto(String wtarget, String wreserve, String wtransit, OrderItemDto orderItemDto, boolean transfer) {
        ShipToCustomerResultDto shipToCustomerResultDto = new ShipToCustomerResultDto();
        shipToCustomerResultDto.setProductOrderItem(orderItemDto);

        WarehouseDto source = createWarehouseDto(wreserve);
        shipToCustomerResultDto.setReserve(source);

        WarehouseDto target = createWarehouseDto(wtarget);
        shipToCustomerResultDto.setTarget(target);
        if (wtransit != null) {
            shipToCustomerResultDto.setTransit(createWarehouseDto(wtransit));
        }
        if (transfer) {
            shipToCustomerResultDto.setTransition(createTransition(source, target));
        }
        return shipToCustomerResultDto;
    }


    private InternalReservationDto createReservation(String wtarget, String wreserve, String wtransit, OrderItemDto itemExternalId1, boolean transfer) {
        InternalReservationDto internalReservationDto = new InternalReservationDto();
        internalReservationDto.setId((long) Math.random());
        internalReservationDto.setProductId(itemExternalId1.getProductId());
        internalReservationDto.setQuantity((double) itemExternalId1.getQuantity());
        io.swagger.client.model.WarehouseDto reserveWarehouseDto = new io.swagger.client.model.WarehouseDto();
        reserveWarehouseDto.setExternalId(wreserve);
        internalReservationDto.setReserveWarehouse(reserveWarehouseDto);
        io.swagger.client.model.WarehouseDto targetWarehouseDto = new io.swagger.client.model.WarehouseDto();
        targetWarehouseDto.setExternalId(wtarget);
        internalReservationDto.setTargetWarehouse(targetWarehouseDto);
        io.swagger.client.model.WarehouseDto transitWarehouseDto = new io.swagger.client.model.WarehouseDto();
        transitWarehouseDto.setExternalId(wtransit);
        internalReservationDto.setTransitWarehouse(transitWarehouseDto);
        if (transfer) {
            internalReservationDto.setTransferStatus(InternalReservationDto.TransferStatusEnum.TRANSFER);
            internalReservationDto.setTransferWarehouseFrom(reserveWarehouseDto);
            internalReservationDto.setTransferWarehouseTo(targetWarehouseDto);
        } else {
            internalReservationDto.setTransferStatus(InternalReservationDto.TransferStatusEnum.NO_TRANSFER);
        }
        return internalReservationDto;
    }


    private TransitionDto createTransition(WarehouseDto from, WarehouseDto to) {
        TransitionDto dto = new TransitionDto();
        dto.setFrom(from);
        dto.setTo(to);
        return dto;
    }

    private WarehouseDto createWarehouseDto(String externalId) {
        if (externalId == null) return null;
        WarehouseDto warehouseDto = new WarehouseDto();
        warehouseDto.setExternalId(externalId);
        return warehouseDto;
    }
}