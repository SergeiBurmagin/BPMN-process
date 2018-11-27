package ru.starfish24.mascotte.bpm.exportreservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.starfish24.mascotte.bpm.exportreservation.dto.LoyaltyCardResponseDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.customercards.CustomerCardsRepository;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.CustomerCard;
import ru.starfish24.starfish24model.ProductOrder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class LoyaltyCardAsyncTask implements JavaDelegate {
    @Autowired
    ProductOrderRepository productOrderRepository;
    @Autowired
    CustomerCardsRepository customerCardsRepository;
    @Autowired
    HttpClient httpClient;
    @Autowired
    IntegrationMicroserviceRepository integrationMicroserviceRepository;

    public LoyaltyCardAsyncTask() {
    }

    public LoyaltyCardAsyncTask(ProductOrderRepository productOrderRepository,
                                CustomerCardsRepository customerCardsRepository,
                                HttpClient httpClient,
                                IntegrationMicroserviceRepository integrationMicroserviceRepository) {
        this.customerCardsRepository = customerCardsRepository;
        this.productOrderRepository = productOrderRepository;
        this.httpClient = httpClient;
        this.integrationMicroserviceRepository = integrationMicroserviceRepository;

    }

    public void execute(DelegateExecution execution) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String externalOrderId = VariableUtils.getStringValue(ProcessVar.ORDER_EXTERNAL_ID, execution);
            ProductOrder productOrder = productOrderRepository.getOneByExternalId(externalOrderId);
            Assert.notNull(productOrder, String.format("productOrderRepository.getOneByExternalId return null" +
                    "with orderExternalId = %s", externalOrderId));

            if (productOrder.getCustomerCard() == null || Strings.isNullOrEmpty(productOrder.getCustomerCard().getExternalId())) {
                log.info("customer card is not present for order = {}", productOrder.getExternalId());
                return;
            }
            String cardExternalId = productOrder.getCustomerCard().getExternalId();

            Set<String> pathVariables = new HashSet<>();
            pathVariables.add(cardExternalId);

            String jsonResponse = httpClient.getRequest(
                    productOrder.getShop().getAccount().getId(),
                    "loyaltycard", pathVariables);

            LoyaltyCardResponseDto loyaltyCardResponseDto =
                    objectMapper.readValue(jsonResponse, LoyaltyCardResponseDto.class);

            CustomerCard card = customerCardsRepository.getFirstByExternalIdEquals(cardExternalId);
            if (card != null) {
                card.setCardNumber(loyaltyCardResponseDto.getNumber());
                card.setAmount(new BigDecimal(loyaltyCardResponseDto.getAmount()));
                card.setTurnover(new BigDecimal(loyaltyCardResponseDto.getTurnover()));
            }

            CustomerCard customerCard = customerCardsRepository.saveAndFlush(card);
            log.info(String.format("Saved loyalty card data { externalId = %s, amount = %s, turnover = %s",
                    customerCard.getExternalId(), customerCard.getAmount(), customerCard.getTurnover()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }
    }
}
