package ru.starfish24.mascotte.service;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationRequest;
import org.junit.Before;
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
import ru.starfish24.mascotte.dto.reservation.ProductRequest;
import ru.starfish24.mascotte.dto.transition.CreateTransitionDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.internalreservation.InternalReservationRepository;
import ru.starfish24.mascotte.repository.product.ProductRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseRepository;
import ru.starfish24.mascotte.service.reservation.ReservationServiceImpl;
import ru.starfish24.starfish24model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceImplTest {

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Mock
    private InternalReservationRepository internalReservationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    InternalReservationControllerApi internalReservationControllerApi;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<InternalReservationRequest> internalReservationRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<Object> objectArgumentCaptor;
    private final String targetExternalId = "targetExternalId";
    private final String transitExternalId = "transitExternalId";
    private final String reserveExternalId = "reserveExternalId";
    private final String fromExternalId = "fromExternalId";
    private final String toExternalId = "toExternalId";
    private Warehouse toWarehouse;
    private Warehouse fromWarehouse;
    private Warehouse reserveWarehouse;
    private Warehouse transitWarehouse;
    private Warehouse targetWarehouse;
    private Long productOrderItemId = 22L;
    private final Long toWarehouseId = 10L;
    ;
    private final Long fromWarehouseId = 20L;
    private final Long resereveWarehouseId = 30L;
    private final Long transitWarehouseId = 40L;
    private final Long targetWarehouseId = 50L;

    @Before
    public void setup() {
        toWarehouse = new Warehouse();
        toWarehouse.setExternalId(toExternalId);
        toWarehouse.setId(toWarehouseId);

        fromWarehouse = new Warehouse();
        fromWarehouse.setExternalId(fromExternalId);
        fromWarehouse.setId(fromWarehouseId);

        reserveWarehouse = new Warehouse();
        reserveWarehouse.setExternalId(reserveExternalId);
        reserveWarehouse.setId(resereveWarehouseId);

        transitWarehouse = new Warehouse();
        transitWarehouse.setExternalId(targetExternalId);
        transitWarehouse.setId(transitWarehouseId);

        targetWarehouse = new Warehouse();
        targetWarehouse.setExternalId(targetExternalId);
        targetWarehouse.setId(targetWarehouseId);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForQuantity() throws IOException {
        ProductOrder productOrder = new ProductOrder();
        ru.starfish24.starfish24model.product.Product product = new ru.starfish24.starfish24model.product.Product();
        product.setId(1L);
        product.setExternalId("prodExtId");
        ProductOrderItem productOrderItem = new ProductOrderItem();
        productOrderItem.setQuantity(2);
        productOrderItem.setProduct(product);
        productOrder.setProductOrderItems(Arrays.asList(productOrderItem));

        Shop shop = new Shop();
        Account account = new Account();
        account.setId(500L);
        shop.setAccount(account);
        productOrder.setShop(shop);

        IntegrationMicroservice integrationMicroservice = new IntegrationMicroservice();
        Product productToReserve = new Product();
        productToReserve.setProductId(1L);
        productToReserve.setQuantity(1);
        List<Product> products = new ArrayList<>();
        products.add(productToReserve);

        InternalReservation activeReservation = new InternalReservation();
        ProductOrderItem activeItemReservation = new ProductOrderItem();
        activeItemReservation.setQuantity(3);

        when(integrationMicroserviceRepository.findByAccountIdAndType(500L, "reservation")).thenReturn(integrationMicroservice);
        when(internalReservationRepository.findByOrderItemId(productOrderItem.getId())).thenReturn(Arrays.asList(activeReservation));

        reservationService.createReservation(1L, products, "2L", "3L", "4L");
    }

    @Test
    public void createTransition() throws IOException {
        Long orderId = 1L;
        String externalOrderId = "externalId";
        ProductOrder productOrder = new ProductOrder();
        productOrder.setExternalId(externalOrderId);
        Shop shop = new Shop();
        Account account = new Account();
        account.setId(1L);
        shop.setAccount(account);
        productOrder.setShop(shop);
        when(productOrderRepository.getOne(orderId)).thenReturn(productOrder);
        IntegrationMicroservice description = new IntegrationMicroservice();
        description.setUri("uri");
        when(integrationMicroserviceRepository.findByAccountIdAndType(eq(account.getId()), eq("transfer"))).thenReturn(description);
        String integrationResponse = "{\"success\": \"true\"}";
        when(httpClient.postRequest(stringArgumentCaptor.capture(), objectArgumentCaptor.capture())).thenReturn(integrationResponse);
        TransitionDto transitionDto = new TransitionDto();
        WarehouseDto wareouseTo = new WarehouseDto();
        wareouseTo.setExternalId("warehouseToExternalId");
        transitionDto.setTo(wareouseTo);
        WarehouseDto warehouseFrom = new WarehouseDto();
        warehouseFrom.setExternalId("warehouseFromExternalId");
        transitionDto.setFrom(warehouseFrom);
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setProductId(2L);
        products.add(product);


        ru.starfish24.starfish24model.product.Product product1 = new ru.starfish24.starfish24model.product.Product();
        product1.setId(2L);
        product1.setExternalId("prodExtId");
        when(productRepository.findOne(anyLong())).thenReturn(product1);
        IntegrationServiceResponse response = reservationService.createTransfer(orderId, products, transitionDto.getTo().getExternalId(), transitionDto.getFrom().getExternalId());
        assertEquals(true, response.getSuccess());
        assertEquals("uri", stringArgumentCaptor.getValue());
        CreateTransitionDto result = (CreateTransitionDto) objectArgumentCaptor.getValue();
        assertEquals(wareouseTo.getExternalId(), result.getTargetWarehouseId());
        assertEquals(warehouseFrom.getExternalId(), result.getWarehouseId());
        assertEquals(1, result.getSkuList().size());
        ProductRequest pr = result.getSkuList().get(0);
        assertEquals("prodExtId", pr.getExternalId());
    }

    @Test
    public void createInternalReservations() {
        List<ShipToCustomerResultDto> shipmentDataInfoes = new ArrayList<>();
        ShipToCustomerResultDto shipmentDataInfo = new ShipToCustomerResultDto();
        TransitionDto transition = new TransitionDto();
        WarehouseDto warehouseDtoTo = new WarehouseDto();
        WarehouseDto warehouseFromDto = new WarehouseDto();
        WarehouseDto warehouseTransitDto = new WarehouseDto();
        WarehouseDto warehouseTargetDto = new WarehouseDto();
        WarehouseDto warehouseReserveDto = new WarehouseDto();
        OrderItemDto productOrderItem = new OrderItemDto();

        warehouseDtoTo.setExternalId(toExternalId);
        warehouseFromDto.setExternalId(fromExternalId);
        warehouseReserveDto.setExternalId(reserveExternalId);
        warehouseTargetDto.setExternalId(targetExternalId);
        warehouseTransitDto.setExternalId(transitExternalId);
        transition.setTo(warehouseDtoTo);
        transition.setFrom(warehouseFromDto);
        productOrderItem.setId(productOrderItemId);
        productOrderItem.setQuantity(10);

        shipmentDataInfo.setTransition(transition);
        shipmentDataInfo.setProductOrderItem(productOrderItem);
        shipmentDataInfo.setTransit(warehouseTransitDto);
        shipmentDataInfo.setTarget(warehouseTargetDto);
        shipmentDataInfo.setReserve(warehouseReserveDto);
        shipmentDataInfoes.add(shipmentDataInfo);
        when(warehouseRepository.findOneByExternalId(targetExternalId)).thenReturn(targetWarehouse);
        when(warehouseRepository.findOneByExternalId(transitExternalId)).thenReturn(transitWarehouse);
        when(warehouseRepository.findOneByExternalId(reserveExternalId)).thenReturn(reserveWarehouse);
        when(warehouseRepository.findOneByExternalId(fromExternalId)).thenReturn(fromWarehouse);
        when(warehouseRepository.findOneByExternalId(toExternalId)).thenReturn(toWarehouse);
        when(internalReservationControllerApi.createResponseUsingPUT(internalReservationRequestArgumentCaptor.capture())).thenReturn(new InternalReservationDto());
        reservationService.createInternalReservations(shipmentDataInfoes);
        verify(internalReservationControllerApi).createResponseUsingPUT(internalReservationRequestArgumentCaptor.capture());
        InternalReservationRequest request = internalReservationRequestArgumentCaptor.getValue();
        assertEquals(productOrderItemId, request.getOrderItemId());
        assertEquals(targetWarehouseId, request.getTargetWarehouseId());
        assertEquals(resereveWarehouseId, request.getReserveWarehouseId());
        assertEquals(transitWarehouseId, request.getTransitWarehouseId());
        assertEquals(10.0, request.getQuantity(), 1);
        assertEquals(fromWarehouseId, request.getTransferFromId());
        assertEquals(toWarehouseId, request.getTransferToId());
    }

    @Test
    public void confirmTransfer() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setId(1L);
        when(internalReservationControllerApi.confirmTransferUsingPOST(1L)).thenReturn(new InternalReservationDto());
        reservationService.confirmInternalTransfer(dto);
        verify(internalReservationControllerApi, atLeastOnce()).confirmTransferUsingPOST(1L);
    }

    @Test
    public void cancelTransfer() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setId(1L);
        when(internalReservationControllerApi.cancelTransferUsingPOST(1L)).thenReturn(new InternalReservationDto());
        reservationService.cancelInternalTransfer(dto);
        verify(internalReservationControllerApi, atLeastOnce()).cancelTransferUsingPOST(1L);
    }

    @Test
    public void confirmReservation() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setId(1L);
        String confirmId = "confirmId";
        when(internalReservationControllerApi.confirmUsingPOST(confirmId, 1L)).thenReturn(new InternalReservationDto());
        reservationService.confirmInternalReservation(dto, confirmId);
        verify(internalReservationControllerApi, atLeastOnce()).confirmUsingPOST(confirmId, 1L);
    }

    @Test
    public void reservationFailed() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setId(1L);
        when(internalReservationControllerApi.failedUsingPOST(1L)).thenReturn(new InternalReservationDto());
        reservationService.internalReservationFailed(dto);
        verify(internalReservationControllerApi, atLeastOnce()).failedUsingPOST(1L);
    }
}
