package ru.starfish24.mascotte.bpm.exportreservation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.dto.exportorder.OrderRequest;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.starfish24model.*;
import ru.starfish24.starfish24model.attributes.order.OrderAttribute;
import ru.starfish24.starfish24model.attributes.order.OrderAttributeValue;
import ru.starfish24.starfish24model.constant.DiscountTypeEnum;
import ru.starfish24.starfish24model.product.Product;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportOrder2ErpActivityTest {

    @InjectMocks
    private ExportOrder2ErpActivity exportOrder2ErpActivity;

    @Mock
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private HttpClient httpClient;

    @Captor
    private ArgumentCaptor<OrderRequest> orderRequestArgumentCaptor;

    @Test
    public void testExportOrder2ErpActivity() throws IOException {
        Integer orderId = 1;
        Long accountId = 424L;
        String microservice = "erp";

        IntegrationMicroservice integrationMicroservice = new IntegrationMicroservice();
        integrationMicroservice.setUri("http://...");
        Account account = new Account();
        account.setId(accountId);
        Shop shop = new Shop();
        shop.setExternalId("shopExtId");
        shop.setAccount(account);

        List<ProductOrderItem> items = new ArrayList<>();
        Product product = new Product();
        product.setExternalId("product");
        ProductOrderItem productOrderItem = new ProductOrderItem();
        productOrderItem.setProduct(product);
        items.add(productOrderItem);

        Phone mainPhone = new Phone();
        mainPhone.setPhone("+79999999999");
        Phone additionalPhone = new Phone();
        additionalPhone.setPhone("+79999999991");

        PaymentType paymentType = new PaymentType();
        paymentType.setExternalId("payment");

        Status status = new Status();
        status.setExternalId("status");

        CancellationReason cancellationReason = new CancellationReason();
        cancellationReason.setExternalId("reason");

        Customer customer = new Customer();
        customer.setLastName("Ivanov");
        customer.setFirstName("Ivan");
        customer.setExternalId("12345");

        CustomerCard customerCard = new CustomerCard();
        customerCard.setExternalId("customerCard");

        List<OrderAttributeValue> orderAttributeList = new ArrayList<>();
        OrderAttribute orderAttribute = new OrderAttribute();
        orderAttribute.setCode("department");
        OrderAttributeValue orderAttributeValue = new OrderAttributeValue();
        orderAttributeValue.setAttribute(orderAttribute);
        orderAttributeValue.setValue("zenden");
        orderAttributeList.add(orderAttributeValue);

        ProductOrder productOrder = new ProductOrder();
        productOrder.setExternalId("orderExtId");
        productOrder.setTimeCreated(new Date());
        productOrder.setShop(shop);
        productOrder.setProductOrderItems(items);
        productOrder.setDiscountValue(new BigDecimal("10"));
        productOrder.setDiscountType(DiscountTypeEnum.ABSOLUT);
        productOrder.setMainPhone(mainPhone);
        productOrder.setAdditionalPhone(additionalPhone);
        productOrder.setSmsRequired(true);
        productOrder.setCallRequired(true);
        productOrder.setCustomerComment("call me later");
        productOrder.setPaymentType(paymentType);
        productOrder.setCanceled(false);
        productOrder.setTimeCanceled(new Date());
        productOrder.setCancelationComment("canceled");
        productOrder.setPaid(true);
        productOrder.setTimePaid(new Date());
        productOrder.setCurrentStatus(status);
        productOrder.setCancellationReason(cancellationReason);
        productOrder.setSumPaid(new BigDecimal("200"));
        productOrder.setCustomerCard(customerCard);
        productOrder.setAttributeValues(orderAttributeList);
        productOrder.setCustomer(customer);

        when(delegateExecution.getVariable("orderId")).thenReturn(orderId);
        when(delegateExecution.getVariableLocal("serviceType")).thenReturn(microservice);
        when(productOrderRepository.findOne(new Long(orderId))).thenReturn(productOrder);
        when(integrationMicroserviceRepository.findByAccountIdAndType(accountId, microservice)).thenReturn(integrationMicroservice);
        when(httpClient.postRequest(anyString(), orderRequestArgumentCaptor.capture())).thenReturn("OK");

        exportOrder2ErpActivity.execute(delegateExecution);

        OrderRequest orderRequest = orderRequestArgumentCaptor.getValue();
        assertEquals(productOrder.getExternalId(), orderRequest.getId());
        assertEquals(shop.getExternalId(), orderRequest.getShop());
        assertEquals(productOrder.getTimeCreated(), orderRequest.getTimeCreated());
        assertEquals(productOrder.getDiscountValue(), orderRequest.getDiscount());
        assertEquals(new Integer(productOrder.getDiscountType().getTypeValue()), orderRequest.getDiscountType());
        assertEquals(productOrder.getMainPhone().getPhone(), orderRequest.getPhone());
        assertEquals(productOrder.getAdditionalPhone().getPhone(), orderRequest.getAdditionalPhone());
        assertTrue(orderRequest.getNeedCall());
        assertTrue(orderRequest.getNeedSms());
        assertEquals(productOrder.getCustomerComment(), orderRequest.getCustomerComment());
        assertEquals(productOrder.getPaymentType().getExternalId(), orderRequest.getPaymentType());
        assertEquals(productOrder.getCanceled(), orderRequest.getIsCanceled());
        assertEquals(productOrder.getTimeCanceled(), orderRequest.getTimeCanceled());
        assertEquals(productOrder.getCancelationComment(), orderRequest.getReasonCanceled());
        assertTrue(orderRequest.getIsPaid());
        assertEquals(productOrder.getTimePaid(), orderRequest.getTimePaid());
        assertEquals(productOrder.getCurrentStatus().getExternalId(), orderRequest.getStatus());
        assertEquals(productOrder.getSumPaid(), orderRequest.getSumPaid());
        assertEquals(productOrder.getCustomerCard().getExternalId(), orderRequest.getLoyaltyCardId());
        assertEquals(1, orderRequest.getItems().size());
        assertEquals(product.getExternalId(), orderRequest.getItems().get(0).getProductId());
        assertNull(orderRequest.getDeliveryDate());
        assertNull(orderRequest.getDeliveryAddress());
        assertNull(orderRequest.getPersonalDiscounts());
        assertEquals(1, orderRequest.getCustomAttributes().size());
        assertEquals(orderAttribute.getCode(), orderRequest.getCustomAttributes().get(0).getName());
        assertEquals(orderAttributeValue.getValue(), orderRequest.getCustomAttributes().get(0).getValue());
        assertEquals(productOrder.getCustomer().getLastName(), orderRequest.getLastName());
        assertEquals(productOrder.getCustomer().getFirstName(), orderRequest.getFirstName());
        assertEquals(productOrder.getCustomer().getExternalId(), orderRequest.getCustomerId());
    }

}
