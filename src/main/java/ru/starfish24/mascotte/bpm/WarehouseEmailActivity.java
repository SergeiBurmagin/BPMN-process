package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.emailtemplate.EmailTemplateRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseAttributeRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseAttributeValueRepository;
import ru.starfish24.mascotte.service.email.EmailService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.attributes.warehouse.WarehouseAttribute;
import ru.starfish24.starfish24model.attributes.warehouse.WarehouseAttributeValue;
import ru.starfish24.starfish24model.tempates.EmailTemplate;

import java.util.Objects;

@Service
@Slf4j
public class WarehouseEmailActivity implements JavaDelegate {

    private final EmailService emailService;

    private final EmailTemplateRepository emailTemplateRepository;

    private final ProductOrderRepository productOrderRepository;

    private final WarehouseAttributeRepository warehouseAttributeRepository;

    private final WarehouseAttributeValueRepository warehouseAttributeValueRepository;

    @Autowired
    public WarehouseEmailActivity(EmailService emailService, EmailTemplateRepository emailTemplateRepository, ProductOrderRepository productOrderRepository, WarehouseAttributeRepository warehouseAttributeRepository, WarehouseAttributeValueRepository warehouseAttributeValueRepository) {
        this.emailService = emailService;
        this.emailTemplateRepository = emailTemplateRepository;
        this.productOrderRepository = productOrderRepository;
        this.warehouseAttributeRepository = warehouseAttributeRepository;
        this.warehouseAttributeValueRepository = warehouseAttributeValueRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, String.format("OrderId cant be NULL!"));
        String emailCode = execution.getVariable(ProcessVar.EMAIL_CODE).toString();
        Long accountId = Long.parseLong(execution.getVariable(ProcessVar.ACCOUNT_ID).toString());

        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        EmailTemplate emailTemplate = emailTemplateRepository.findByCodeAndAccount(emailCode, accountId);
        Objects.requireNonNull(emailTemplate,
                String.format("email template[code: %s accountId: %s] NOT FOUND", emailCode, accountId)
        );

        String recipient = execution.getVariable("recipient").toString();
        Objects.requireNonNull(recipient, "Don`t set  recipient type ");

        String emailAddressString = getEmailsByRecipientType(productOrder, recipient, productOrder.getShop().getAccount().getId());
        String text = TemplateUtils.fillTemplate(emailTemplate.getText(), productOrder, "");
        String subject = TemplateUtils.fillSubject(emailTemplate.getTheme(), productOrder);

        emailService.sendMessage(emailAddressString, subject, text);
    }

    private String getEmailsByRecipientType(ProductOrder productOrder, String code, Long accountId) {
        Long warehouseId = productOrder.getShippings().get(0).getWarehouse().getId();
        log.debug("warehouse id = {}", warehouseId);
        WarehouseAttribute warehouseAttribute = warehouseAttributeRepository.findByCode(
                code, accountId
        );
        log.debug("Get warehouseAttribute {}", warehouseAttribute);
        WarehouseAttributeValue warehouseAttributeValue = warehouseAttributeValueRepository.findByCodeObject(
                code, warehouseId
        );
        log.debug("Get warehouseAttribute value = {}", warehouseAttributeValue);
        return warehouseAttributeValue.getValue();
    }
}
