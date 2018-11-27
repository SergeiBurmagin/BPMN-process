package ru.starfish24.mascotte.bpm.exportreservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.Account;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderBrockerActivityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DelegateExecution delegateExecution;

    @Captor
    private ArgumentCaptor<List<ShipToCustomerResultDto>> listArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> uriArgumentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, String>> paramArgumentCaptor;

    private CalculateReservationRoadmapActivity orderBrockerActivity;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;


    @Before
    public void setup() {
        orderBrockerActivity =
                new CalculateReservationRoadmapActivity(httpClient, productOrderRepository, integrationMicroserviceRepository);
    }

    @Test
    public void execute() throws Exception {

        Integer orderId = 1;
        Long longOrderId = 1L;
        String externalId = "externalid";
        String uri = "testuri";
        Long accountId = 33L;

        ProductOrder productOrder = new ProductOrder();
        productOrder.setId(longOrderId);
        productOrder.setExternalId(externalId);
        Shop shop = new Shop();
        Account account = new Account();

        account.setId(accountId);
        shop.setAccount(account);
        productOrder.setShop(shop);
        when(delegateExecution.getVariable(ProcessVar.ORDER_ID)).thenReturn(orderId);
        doNothing().when(delegateExecution).setVariable(eq(ProcessVar.RESERVATION_ROADMAP), any());
        when(productOrderRepository.getOne(longOrderId)).thenReturn(productOrder);
        List<ShipToCustomerResultDto> result = new ArrayList<>();
        result.add(new ShipToCustomerResultDto());
        IntegrationMicroservice integrationMicroservice = new IntegrationMicroservice();
        integrationMicroservice.setUri(uri);
        when(integrationMicroserviceRepository.findByAccountIdAndType(accountId, "orderBroker")).thenReturn(integrationMicroservice);
        when(httpClient.getRequest(anyString(), anyMap())).thenReturn(objectMapper.writeValueAsString(result));
        orderBrockerActivity.execute(delegateExecution);

        verify(httpClient, times(1)).getRequest(uriArgumentCaptor.capture(), paramArgumentCaptor.capture());
        assertEquals(uri, uriArgumentCaptor.getValue());
        assertTrue(paramArgumentCaptor.getValue().containsKey("orderId"));
        assertEquals(externalId, paramArgumentCaptor.getValue().get("orderId"));


        verify(delegateExecution, times(1)).setVariable(eq(ProcessVar.RESERVATION_ROADMAP), listArgumentCaptor.capture());
        assertEquals(1, listArgumentCaptor.getValue().size());

    }
}