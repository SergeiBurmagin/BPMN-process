package ru.starfish24.mascotte;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.emailtemplate.EmailTemplateRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.email.AmazonSEService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.tempates.EmailTemplate;

import java.util.Objects;

@Service
@Slf4j
public class CustomerEmailActivity implements JavaDelegate {
    private final AmazonSEService amazonSEService;

    private final EmailTemplateRepository emailTemplateRepository;

    private final ProductOrderRepository productOrderRepository;

    @Autowired
    public CustomerEmailActivity(AmazonSEService amazonSEService, EmailTemplateRepository emailTemplateRepository, ProductOrderRepository productOrderRepository) {
        this.amazonSEService = amazonSEService;
        this.emailTemplateRepository = emailTemplateRepository;
        this.productOrderRepository = productOrderRepository;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String departmentGroup = delegateExecution.getVariable(ProcessVar.DEPARTMENT) != null ? delegateExecution.getVariable(ProcessVar.DEPARTMENT).toString() : null;
        Long accountId = new Long(delegateExecution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        String attachment = delegateExecution.getVariable(ProcessVar.ATTACHMENT) != null ? delegateExecution.getVariable(ProcessVar.ATTACHMENT).toString() : null;
        String emailCode = delegateExecution.getVariable(ProcessVar.EMAIL_CODE).toString();
        Long orderId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        Objects.requireNonNull(orderId, "Process variable orderId is not set ");
        EmailTemplate emailTemplate = emailTemplateRepository.findByCodeAndAccount(emailCode, accountId);

        if (emailTemplate == null) {
            log.error("email template[code: {} accountId: {}] NOT FOUND", emailCode, accountId);
            return;
        }

        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        if (productOrder == null) {
            log.error("NO order find by id =  {}", orderId);
            return;
        }

        String text = TemplateUtils.fillTemplate(emailTemplate.getText(), productOrder, departmentGroup);
        String subject = TemplateUtils.fillSubject(emailTemplate.getTheme(), productOrder);

        amazonSEService.sendEmail(productOrder.getEmail().getEmail(), subject, text);
    }
}
