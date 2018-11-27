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
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.TemplateUtils;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.Phone;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.ProductOrderItem;
import ru.starfish24.starfish24model.attributes.product.ProductAttributeValue;
import ru.starfish24.starfish24model.tempates.SmsTemplate;

import java.util.*;

@Service
@Slf4j
public class CustomerSmsActivity implements JavaDelegate {

    private static final String DEPARTMENT = "DEPARTMENT";

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private HttpClient httpClient;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Long accountId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ACCOUNT_ID).toString());
        String smsCode = delegateExecution.getVariableLocal(ProcessVar.SMS_CODE).toString();
        SmsTemplate smsTemplate = smsTemplateRepository.findByAccountIdAndCode(accountId, smsCode);

        if (smsTemplate == null) {
            log.info("EXCLUDE. SMS Template [code = '{}', accountId = '{}'] NOT FOUND", smsCode, accountId);
            return;
        }
        if (smsTemplate == null) {
            log.info("EXCLUDE. SMS Template [code = '{}', accountId = '{}'] NOT FOUND", smsCode, accountId);
            return;
        }

        Long orderId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());

		ProductOrder productOrder = productOrderRepository.findOne(orderId);

		List<String> departmentsList = new ArrayList<>();
		if (delegateExecution.hasVariable(ProcessVar.DEPARTMENT)) {
			departmentsList = (ArrayList<String>) delegateExecution.getVariable(ProcessVar.DEPARTMENT);
		} else {
			for (ProductOrderItem item : productOrder.getProductOrderItems()) {
				Optional<ProductAttributeValue> attrVal = item.getProduct().getProductAttributeValues().stream().filter(attr -> Objects.equals(DEPARTMENT, attr.getAttribute().getCode())).findFirst();
				if (attrVal.isPresent()) {
					departmentsList.add(attrVal.get().getValue());
				}
			}
		}

		Set<String> departmentCollection = new HashSet<>(departmentsList);
		String departments = String.join(", ", departmentCollection);

		Phone phone = productOrder.getMainPhone();
		String mainPhone;
		if (phone == null || (mainPhone = phone.getPhone()) == null) {
			log.info("EXCLUDE. Main phone [orderId = '{}'] NOT EXIST", orderId);
			return;
		}

		String text = TemplateUtils.fillTemplate(smsTemplate.getText(), productOrder, departments);

		IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "sms");
		if (integrationMicroservice == null) {
			log.error("integration microservice [type = 'sms', accountId = {}] NOT FOUND", accountId);
			return;
		}

		SmsRequest smsRequest = new SmsRequest();
		smsRequest.setMessage(text);
		smsRequest.setRecipient(mainPhone);
		smsRequest.setClientId(integrationMicroservice.getClientId());
		smsRequest.setSecretKey(integrationMicroservice.getSecretKey());
		try {
            log.info("sending SMS request {}", smsRequest);
			httpClient.postRequest(integrationMicroservice.getUri(), smsRequest);
        } catch (Exception e) {
            log.error("can't send sms to {}: {} with request = {}", integrationMicroservice.getUri(),
                    e.getMessage(), smsRequest);
		}
	}


}
