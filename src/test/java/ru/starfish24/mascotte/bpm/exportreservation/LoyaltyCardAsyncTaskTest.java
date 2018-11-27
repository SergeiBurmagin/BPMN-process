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
import ru.starfish24.mascotte.bpm.exportreservation.dto.LoyaltyCardResponseDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.customercards.CustomerCardsRepository;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

//@Deployment(resources = { "processes/export-reservation-process.bpmn" })
@RunWith(MockitoJUnitRunner.class)
/**
 * @id Task_1my8954
 */
public class LoyaltyCardAsyncTaskTest {


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DelegateExecution delegateExecution;

    @Captor
    private ArgumentCaptor<Map<String, String>> paramArgumentCaptor;

    @Mock
    private HttpClient httpClient;
    @Mock
    private CustomerCardsRepository customerCardsRepository;
    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    LoyaltyCardAsyncTask loyaltyCardAsyncTask;

    @Before
    public void setup() {
        loyaltyCardAsyncTask =
                new LoyaltyCardAsyncTask(productOrderRepository, customerCardsRepository,
                        httpClient, integrationMicroserviceRepository);

    }


    @Test
    public void execute() throws Exception {

        Integer orderId = 1;
        Long longOrderId = 1L;
        String externalId = "11687415";
        String uri = "testuri";
        Long accountId = 33L;
        String cardExternalId = "eee-444";
        Long cardId = 1L;

        ProductOrder productOrder = new ProductOrder();
        productOrder.setId(longOrderId);
        productOrder.setExternalId(externalId);

        CustomerCard card = new CustomerCard();
        card.setId(cardId);
        card.setExternalId(cardExternalId);
        productOrder.setCustomerCard(card);

        Account account = new Account();
        account.setId(33L);
        Shop shop = new Shop();
        shop.setAccount(account);
        productOrder.setShop(shop);
        CustomerCard customerCard = new CustomerCard();
        customerCard.setCardNumber("0088800003400054");
        customerCard.setAmount(new BigDecimal("2005"));
        customerCard.setTurnover(new BigDecimal("17000"));
        when(delegateExecution.getVariableTyped(ProcessVar.ORDER_EXTERNAL_ID))
                .thenReturn(Variables.stringValue(externalId));

        when(productOrderRepository.getOneByExternalId(externalId)).thenReturn(productOrder);

        IntegrationMicroservice integrationMicroservice = new IntegrationMicroservice();
        integrationMicroservice.setUri(uri);
        when(integrationMicroserviceRepository.findByAccountIdAndType(accountId, "loyaltycard"))
                .thenReturn(integrationMicroservice);

        when(customerCardsRepository.getFirstByExternalIdEquals(cardExternalId)).thenReturn(card);

        LoyaltyCardResponseDto result =
                new LoyaltyCardResponseDto("0010402594212", "0", "1290");

        when(httpClient.getRequest(anyLong(), anyString(), anySet()))
                .thenReturn(objectMapper.writeValueAsString(result));

        when(customerCardsRepository.saveAndFlush(any())).thenReturn(customerCard);

        loyaltyCardAsyncTask.execute(delegateExecution);
    }

}
