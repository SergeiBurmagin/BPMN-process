package ru.starfish24.mascotte.bpm.exportreservation;

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
public class CheckItemStatusActivity implements JavaDelegate {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Override
    public void execute(DelegateExecution execution) {
        String externalOrderId = VariableUtils.getStringValue(ProcessVar.ORDER_EXTERNAL_ID, execution);
        ProductOrder productOrder = productOrderRepository.getOneByExternalId(externalOrderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        log.info("check item status [externalOrderId = {}]", externalOrderId);

        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "itemstatus");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'itemstatus', accountId = {}] NOT FOUND", accountId);
            return;
        }
        httpClient.getRequest(String.format("%s/%s", integrationMicroservice.getUri(), externalOrderId));
    }
}
