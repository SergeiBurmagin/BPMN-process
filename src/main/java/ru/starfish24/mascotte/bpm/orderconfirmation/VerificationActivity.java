package ru.starfish24.mascotte.bpm.orderconfirmation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;

@Service
@Slf4j
public class VerificationActivity implements JavaDelegate {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = new Long(VariableUtils.getIntOrThrow(ProcessVar.ORDER_ID, execution));
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        log.info("start order verification [externalId = {}]", productOrder.getExternalId());

        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "verification");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'verification', accountId = {}] NOT FOUND", accountId);
            return;
        }
        httpClient.getRequest(String.format("%s/%s", integrationMicroservice.getUri(), productOrder.getExternalId()));
    }
}
