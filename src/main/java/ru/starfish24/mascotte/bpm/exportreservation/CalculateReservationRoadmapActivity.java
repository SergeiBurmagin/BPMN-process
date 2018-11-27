package ru.starfish24.mascotte.bpm.exportreservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.HashMap;
import java.util.List;

/**
 * Получим данные из модуля order-broker по резервации и перемещениям и создадим карту RESERVATION_ROADMAP
 */
@Service
@Slf4j
public class CalculateReservationRoadmapActivity implements JavaDelegate {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;

    private final ProductOrderRepository productOrderRepository;

    private final IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    public CalculateReservationRoadmapActivity(HttpClient httpClient, ProductOrderRepository productOrderRepository, IntegrationMicroserviceRepository integrationMicroserviceRepository) {
        this.httpClient = httpClient;
        this.productOrderRepository = productOrderRepository;
        this.integrationMicroserviceRepository = integrationMicroserviceRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        final Long orderId = Long.valueOf(execution.getVariable(ProcessVar.ORDER_ID).toString());
        log.info("Invoked with orderId = {}", orderId);
        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        Assert.notNull(productOrder.getExternalId(), String.format("Order externalId cant be null, [orderId=%s]", productOrder.getId()));
        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(productOrder.getShop().getAccount().getId(), "orderBroker");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'orderBroker', accountId = {}] NOT FOUND", productOrder.getShop().getAccount().getId());
            return;
        }
        String json = httpClient.getRequest(integrationMicroservice.getUri(),
                new HashMap<String, String>() {{
                    put("orderId", productOrder.getExternalId());
                }}
        );
        List<ShipToCustomerResultDto> reservation = MAPPER.readValue(json, new TypeReference<List<ShipToCustomerResultDto>>() {
        });

        execution.setVariableLocal("hasReservation", validResponse(reservation));

        execution.setVariable(ProcessVar.RESERVATION_ROADMAP, reservation);
    }

    private boolean validResponse(List<ShipToCustomerResultDto> in) {
        if (in != null && !in.isEmpty())
            return true;
        return false;
    }
}
