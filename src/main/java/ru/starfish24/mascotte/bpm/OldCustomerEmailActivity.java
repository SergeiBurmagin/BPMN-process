package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.emailtemplate.EmailTemplateRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.email.EmailService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.tempates.EmailTemplate;

@Service
@Slf4j
public class OldCustomerEmailActivity implements JavaDelegate {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    public void execute(DelegateExecution execution) throws Exception {
        Long accountId = Long.parseLong(execution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);

        String departmentCollection = "";
        if (execution.hasVariable(ProcessVar.DEPARTMENT_COLL)) {
            execution.getVariable(ProcessVar.DEPARTMENT_COLL).toString();
        }

        String emailCode = execution.getVariableLocal(ProcessVar.EMAIL_CODE).toString();
        EmailTemplate emailTemplate = emailTemplateRepository.findByCodeAndAccount(emailCode, accountId);

        if (emailTemplate != null && productOrder.getEmail() != null) {
            String text = TemplateUtils.fillTemplate(emailTemplate.getText(), productOrder, departmentCollection);
            emailService.sendMessage(productOrder.getEmail().getEmail(), emailTemplate.getTheme(), text);
        }
    }
}
