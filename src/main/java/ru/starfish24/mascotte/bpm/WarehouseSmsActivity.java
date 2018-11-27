package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.SmsRequest;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.smstemplate.SmsTemplateRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseAttributeRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseAttributeValueRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.attributes.warehouse.WarehouseAttributeValue;
import ru.starfish24.starfish24model.tempates.SmsTemplate;

import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
public class WarehouseSmsActivity implements JavaDelegate {

    private final SmsTemplateRepository smsTemplateRepository;

    private final ProductOrderRepository productOrderRepository;

    private final WarehouseAttributeRepository warehouseAttributeRepository;

    private final WarehouseAttributeValueRepository warehouseAttributeValueRepository;

    private final IntegrationMicroserviceRepository integrationMicroserviceRepository;

    private final HttpClient httpClient;

    @Autowired
    public WarehouseSmsActivity(ProductOrderRepository productOrderRepository, WarehouseAttributeRepository warehouseAttributeRepository, WarehouseAttributeValueRepository warehouseAttributeValueRepository, SmsTemplateRepository smsTemplateRepository, IntegrationMicroserviceRepository integrationMicroserviceRepository, HttpClient httpClient) {
        this.smsTemplateRepository = smsTemplateRepository;
        this.productOrderRepository = productOrderRepository;
        this.warehouseAttributeRepository = warehouseAttributeRepository;
        this.warehouseAttributeValueRepository = warehouseAttributeValueRepository;
        this.httpClient = httpClient;
        this.integrationMicroserviceRepository = integrationMicroserviceRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, String.format("OrderId cant be NULL!"));
        String smsCode = execution.getVariable(ProcessVar.SMS_CODE).toString();
        Long accountId = Long.parseLong(execution.getVariable(ProcessVar.ACCOUNT_ID).toString());

        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        SmsTemplate smsTemplate = smsTemplateRepository.findByAccountIdAndCode(accountId, smsCode);
        Objects.requireNonNull(smsTemplate,
                String.format("sms template[code: %s accountId: %s] NOT FOUND", smsCode, accountId)
        );

        String recipient = execution.getVariable("recipient").toString();
        Objects.requireNonNull(recipient, "Don`t set  recipient type ");

        String phone = getPhoneByRecipientType(productOrder, recipient, productOrder.getShop().getAccount().getId());
        String text = TemplateUtils.fillTemplate(smsTemplate.getText(), productOrder, "");

        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "sms");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'sms', accountId = {}] NOT FOUND", accountId);
            return;
        }

        sendSms(integrationMicroservice, phone, text);
    }

    private void sendSms(IntegrationMicroservice integrationMicroservice, String phone, String text) {
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setMessage(text);
        smsRequest.setRecipient(phone);
        smsRequest.setSendAfter("09:00");
        smsRequest.setSendBefore("19:00");
        smsRequest.setClientId(integrationMicroservice.getClientId());
        smsRequest.setSecretKey(integrationMicroservice.getSecretKey());
        try {
            httpClient.postRequest(integrationMicroservice.getUri(), smsRequest);
        } catch (IOException e) {
            log.error("can't send sms to {}: {}", integrationMicroservice.getUri(), e.getMessage());
        }
    }

    private String getPhoneByRecipientType(ProductOrder productOrder, String code, Long accountId) {
        Long warehouseId = productOrder.getShippings().get(0).getWarehouse().getId();
        log.debug("warehouse id = {}", warehouseId);
        WarehouseAttributeValue warehouseAttributeValue = warehouseAttributeValueRepository.findByCodeObject(
                code, warehouseId
        );
        log.debug("Get warehouseAttribute value = {}", warehouseAttributeValue);
        return warehouseAttributeValue.getValue();
    }
}
