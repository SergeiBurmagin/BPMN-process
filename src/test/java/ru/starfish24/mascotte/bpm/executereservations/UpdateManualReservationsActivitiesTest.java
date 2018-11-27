package ru.starfish24.mascotte.bpm.executereservations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.WarehouseDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.TransitionDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.starfish24model.Account;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateManualReservationsActivitiesTest {


    @Mock
    private ExecuteReservationService executeReservationService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private HttpClient httpClient;


    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private DelegateExecution delegateExecution;

    @Captor
    private ArgumentCaptor<Map<Long, String>> mapArgumentCaptor;


    private UpdateManualReservationsActivities updateManualReservationsActivities;

    @Before
    public void setup() {
        updateManualReservationsActivities = new UpdateManualReservationsActivities(executeReservationService,
                reservationService,
                integrationMicroserviceRepository,
                productOrderRepository,
                httpClient,
                mapper);
    }


    @Test
    public void execute() throws Exception {

        Long manualReserveId = 1L;
        Long commonReserveId = 2L;
        Long orderId = 12L;
        Long reserveWarehouseId = 100L;
        Long orderItemId = 112L;
        Long accountId = 111L;
        Long transitWarehouseId = 113L;
        Long targetWarehouseId = 114L;
        Long fromWarehouseId = 115L;
        Long toWarehouseId = 116L;
        when(delegateExecution.getVariable("orderId")).thenReturn(orderId);
        Map<Long, String> toExecute = new HashMap<>();
        toExecute.put(manualReserveId, String.valueOf(manualReserveId));
        toExecute.put(commonReserveId, String.valueOf(commonReserveId));
        when(delegateExecution.getVariable("toExecute")).thenReturn(toExecute);
        InternalReservationDto manualReserve = new InternalReservationDto();
        manualReserve.setId(manualReserveId);
        manualReserve.setStatus(InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE);

        manualReserve.setOrderItemId(orderItemId);
        WarehouseDto reserveWarehouse = new WarehouseDto();
        reserveWarehouse.setId(reserveWarehouseId);
        manualReserve.setReserveWarehouse(reserveWarehouse);
        ;
        InternalReservationDto commonReserve = new InternalReservationDto();
        commonReserve.setId(commonReserveId);
        commonReserve.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);

        when(executeReservationService.toDto(String.valueOf(manualReserveId))).thenReturn(manualReserve);
        when(executeReservationService.toDto(String.valueOf(commonReserveId))).thenReturn(commonReserve);

        ProductOrder productOrder = new ProductOrder();
        Shop shop = new Shop();
        Account account = new Account();
        account.setId(accountId);
        shop.setAccount(account);
        productOrder.setShop(shop);
        when(productOrderRepository.getOne(orderId)).thenReturn(productOrder);
        IntegrationMicroservice serviceInfo = new IntegrationMicroservice();
        serviceInfo.setUri("uri");
        when(integrationMicroserviceRepository.findByAccountIdAndType(accountId, "orderBroker")).thenReturn(serviceInfo);
        List<ShipToCustomerResultDto> shippingInfoes = new ArrayList<>();
        ShipToCustomerResultDto shippingInfo = new ShipToCustomerResultDto();
        shippingInfo.setReserve(createWarehouseDto(reserveWarehouseId));

        shippingInfo.setTarget(createWarehouseDto(targetWarehouseId));

        shippingInfo.setTransit(createWarehouseDto(transitWarehouseId));
        TransitionDto transition = new TransitionDto();

        transition.setFrom(createWarehouseDto(fromWarehouseId));

        transition.setTo(createWarehouseDto(toWarehouseId));
        shippingInfo.setTransition(transition);
        shippingInfoes.add(shippingInfo);
        String stringResult = createResult(shippingInfoes);
        when(httpClient.getRequest(eq(serviceInfo.getUri()), any())).thenReturn(stringResult);
        when(executeReservationService.toString(any())).thenAnswer(args -> mapper.writeValueAsString(args.getArguments()[0]));
        when(reservationService.getWarehouseId(any())).thenAnswer(args ->
                Long.valueOf(((ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto) args.getArguments()[0]).getExternalId()));

        updateManualReservationsActivities.execute(delegateExecution);

        Map<Long, String> vars = toExecute;
        InternalReservationDto internalReservationDto = mapper.readValue(vars.get(manualReserveId), InternalReservationDto.class);
        assertEquals(fromWarehouseId, internalReservationDto.getTransferWarehouseFrom().getId());
        assertEquals(toWarehouseId, internalReservationDto.getTransferWarehouseTo().getId());
        assertEquals(reserveWarehouseId, internalReservationDto.getReserveWarehouse().getId());
        assertEquals(transitWarehouseId, internalReservationDto.getTransitWarehouse().getId());
        assertEquals(targetWarehouseId, internalReservationDto.getTargetWarehouse().getId());
    }

    private ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto createWarehouseDto(long id) {
        ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto warehouseDto = new ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto();
        warehouseDto.setExternalId(String.valueOf(id));
        return warehouseDto;
    }

    private String createResult(List<ShipToCustomerResultDto> shippingInfo) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(shippingInfo);
    }
}