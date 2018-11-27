package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.dto.SmsRequest;
import ru.starfish24.mascotte.repository.employee.EmployeeRepository;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.smstemplate.SmsTemplateRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.mascotte.utils.Utils;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.Employee;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.tempates.SmsTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SmsServiceTaskActivity implements JavaDelegate {

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private HttpClient httpClient;

    public void execute(DelegateExecution delegateExecution) {
        Long accountId = VariableUtils.getLongOrThrow(ProcessVar.ACCOUNT_ID, delegateExecution);
        Long shopId = VariableUtils.getLongOrThrow(ProcessVar.SHOP_ID, delegateExecution);
        Long orderId = VariableUtils.getLongOrThrow(ProcessVar.ORDER_ID, delegateExecution);

        Object localGroupCode = delegateExecution.getVariable(ProcessVar.GROUP_CODE);
        Object departmentGroup = delegateExecution.getVariable(ProcessVar.DEPARTMENT);
        String smsCode = delegateExecution.getVariableLocal(ProcessVar.SMS_CODE).toString();
        Object startDayVar = delegateExecution.getVariableLocal("startDay");
        Object endDayVar = delegateExecution.getVariableLocal("endDay");

        //Object smsSendDateVar = delegateExecution.getVariableLocal(ProcessVar.SMS_SEND_DATE);
        //Date smsSendDate = smsSendDateVar != null ? (Date) smsSendDateVar : null;

        SmsTemplate smsTemplateEntity = smsTemplateRepository.findByAccountIdAndCode(accountId, smsCode);
        if (smsTemplateEntity == null) {
            log.error("SMS template [code = {}, accountId = {}] NOT FOUND", smsCode, accountId);
            return;
        }

        String groupCode = localGroupCode == null ? departmentGroup.toString() : localGroupCode.toString();
        List<Employee> employees = employeeRepository.findByGroupShop(accountId, shopId, groupCode);
        if (Utils.isEmpty(employees)) {
            log.warn("skip send SMS. No employees of group = {}, accountId = {}", groupCode, accountId);
            return;
        }

        Set<String> phones = employees.stream().map(Employee::getPhone).filter(s -> !Utils.isEmpty(s)).collect(Collectors.toSet());

        if (Utils.isEmpty(phones)) {
            String login = Utils.stream(employees).map(e -> String.format("{id:%s, login:%s}", e.getId(), e.getLogin())).reduce((s, s2) -> s + ", " + s2).get();
            log.warn("skip send SMS. No phones found of employees [department = {}] : {}", groupCode, login);
            return;
        }

        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        String text = TemplateUtils.fillTemplate(smsTemplateEntity.getText(), productOrder, groupCode);

        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "sms");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'sms', accountId = {}] NOT FOUND", accountId);
            return;
        }

        phones.forEach(phone -> {
            SmsRequest smsRequest = new SmsRequest();
            smsRequest.setMessage(text);
            smsRequest.setRecipient(phone);
            smsRequest.setSendAfter(startDayVar != null ? startDayVar.toString() + ":00" : null);
            smsRequest.setSendBefore(endDayVar != null ? endDayVar.toString() + ":00" : null);
            smsRequest.setClientId(integrationMicroservice.getClientId());
            smsRequest.setSecretKey(integrationMicroservice.getSecretKey());
            try {
                httpClient.postRequest(integrationMicroservice.getUri(), smsRequest);
            } catch (IOException e) {
                log.error("can't send sms to {}: {}", integrationMicroservice.getUri(), e.getMessage());
            }
        });
    }

}
