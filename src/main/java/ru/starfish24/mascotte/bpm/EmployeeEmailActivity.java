package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.emailtemplate.EmailTemplateRepository;
import ru.starfish24.mascotte.repository.employee.EmployeeRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.email.EmailService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.mascotte.utils.Utils;
import ru.starfish24.starfish24model.Employee;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.tempates.EmailTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class EmployeeEmailActivity implements JavaDelegate {

    private final EmailService emailService;

    private final EmailTemplateRepository emailTemplateRepository;

    private final ProductOrderRepository productOrderRepository;

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeEmailActivity(EmailService emailService, EmailTemplateRepository emailTemplateRepository, ProductOrderRepository productOrderRepository, EmployeeRepository employeeRepository) {
        this.emailService = emailService;
        this.emailTemplateRepository = emailTemplateRepository;
        this.productOrderRepository = productOrderRepository;
        this.employeeRepository = employeeRepository;
    }


    private List<Employee> findEmployees(Long accountId, Long shopId, Object departmentGroup, Object localGroupCode) {
        String code = localGroupCode == null ? departmentGroup.toString() : localGroupCode.toString();

        List<Employee> employees;
        if (shopId == null) {
            employees = employeeRepository.findByGroupCode(accountId, code);
        } else {
            employees = employeeRepository.findByGroupShop(accountId, shopId, code);
        }
        log.info("Find {} employees [accountId: {}, shopId: {}, groupCode: '%s'(%s)] : %s", employees.size(), accountId, shopId, code,
                localGroupCode == null ? "department" : "local",
                employees.stream().map(e -> String.format("'%s'", e.getLogin())).collect(Collectors.joining(", ")));
        return employees;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String localGroupCode = delegateExecution.getVariable(ProcessVar.GROUP_CODE) != null ? delegateExecution.getVariable(ProcessVar.GROUP_CODE).toString() : null;
        String departmentGroup = delegateExecution.getVariable(ProcessVar.DEPARTMENT) != null ? delegateExecution.getVariable(ProcessVar.DEPARTMENT).toString() : null;
        Long accountId = new Long(delegateExecution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        String attachment = delegateExecution.getVariable(ProcessVar.ATTACHMENT) != null ? delegateExecution.getVariable(ProcessVar.ATTACHMENT).toString() : null;
        String emailCode = delegateExecution.getVariable(ProcessVar.EMAIL_CODE).toString();
        EmailTemplate emailTemplate = emailTemplateRepository.findByCodeAndAccount(emailCode, accountId);
        if (emailTemplate == null) {
            log.error("email template[code: {} accountId: {}] NOT FOUND", emailCode, accountId);
            return;
        }

        Long shopId = new Long(delegateExecution.getVariable(ProcessVar.SHOP_ID).toString());
        List<Employee> employees = findEmployees(accountId, shopId, departmentGroup, localGroupCode);

        if (employees.isEmpty()) {
            String groups = Stream.of(departmentGroup, localGroupCode).filter(Objects::nonNull).collect(Collectors.joining(", "));
            log.warn("SKIP | No employees of group[codes: %s]'", groups);
            return;
        }
        String addresses = employees.stream().map(Employee::getEmail)
                .filter(s -> s != null && !s.isEmpty() && Utils.isValidEmailAddress(s))
                .collect(Collectors.joining(","));

        if (addresses.isEmpty()) {
            String employeesInfo = employees.stream().map(e -> String.format("{id:%s, login:%s}", e.getId(), e.getLogin())).reduce((s, s2) -> s + ", " + s2).get();
            log.warn("SKIP | No emails of employees {}", employeesInfo);
            return;
        }

        if (delegateExecution.getVariable(ProcessVar.ERROR_MESSAGE) != null) {
            String jsonText = delegateExecution.getVariable(ProcessVar.JSON_TEXT).toString();
            String errorMessage = delegateExecution.getVariable(ProcessVar.ERROR_MESSAGE).toString();
            String template = emailTemplate.getText();
            template = template.replaceAll("\\{ACCOUNTID\\}", accountId.toString());
            if (jsonText != null && jsonText.length() > 4000) {
                jsonText = jsonText.substring(0, 3999);
            }
            template = template.replaceAll("\\{INCOMEJSON\\}", jsonText);
            template = template.replaceAll("\\{ERRORVALUE\\}", errorMessage);
            emailService.sendMessage(addresses, emailTemplate.getTheme(), template);
            return;
        }

        Long orderId = new Long(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        String text = emailTemplate.getText();
        String subject = emailTemplate.getTheme();

        if (orderId != null) {
            ProductOrder productOrder = productOrderRepository.findOne(orderId);
            if (productOrder == null) {
                log.error("NO order find by id =  {}", orderId);
                return;
            }
            text = TemplateUtils.fillTemplate(emailTemplate.getText(), productOrder, departmentGroup);
            subject = TemplateUtils.fillSubject(emailTemplate.getTheme(), productOrder);
        }

        if (Utils.isNotEmpty(attachment)) {
            emailService.sendMessage(addresses, subject, text, attachment);
        } else {
            emailService.sendMessage(addresses, subject, text);
        }
    }
}
