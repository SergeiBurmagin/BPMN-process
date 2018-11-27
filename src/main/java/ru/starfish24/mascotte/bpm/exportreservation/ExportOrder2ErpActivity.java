package ru.starfish24.mascotte.bpm.exportreservation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.dto.exportorder.*;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.mascotte.utils.VariableUtils;
import ru.starfish24.starfish24model.*;
import ru.starfish24.starfish24model.attributes._common.AbstractAttribute;
import ru.starfish24.starfish24model.attributes._common.AbstractAttributeValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExportOrder2ErpActivity implements JavaDelegate {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private HttpClient httpClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws IOException {
        Long orderId = new Long(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        String serviceType = VariableUtils.getLocalStringValue("serviceType", delegateExecution);
        Object cancelReservation = delegateExecution.getVariable("cancelReservation");

        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, serviceType);
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'erp', accountId = {}] NOT FOUND", accountId);
            return;
        }

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setId(productOrder.getExternalId());
        orderRequest.setShop(productOrder.getShop().getExternalId());
        orderRequest.setTimeCreated(productOrder.getTimeCreated());
        orderRequest.setDiscount(productOrder.getDiscountValue());
        orderRequest.setDiscountType(productOrder.getDiscountType() != null ? productOrder.getDiscountType().getTypeValue() : null);
        orderRequest.setPhone(productOrder.getMainPhone() != null ? productOrder.getMainPhone().getPhone() : null);
        orderRequest.setAdditionalPhone(productOrder.getAdditionalPhone() != null ? productOrder.getAdditionalPhone().getPhone() : null);
        orderRequest.setNeedCall(productOrder.getCallRequired());
        orderRequest.setNeedSms(productOrder.getSmsRequired());
        orderRequest.setCustomerComment(productOrder.getCustomerComment());
        orderRequest.setPaymentType(productOrder.getPaymentType() != null ? productOrder.getPaymentType().getExternalId() : null);
        orderRequest.setIsCanceled(productOrder.getCanceled());
        orderRequest.setTimeCanceled(productOrder.getTimeCanceled());
        orderRequest.setReasonCanceled(productOrder.getCancelationComment());
        orderRequest.setIsPaid(productOrder.getPaid());
        orderRequest.setTimePaid(productOrder.getTimePaid());
        orderRequest.setStatus(productOrder.getCurrentStatus() != null ? productOrder.getCurrentStatus().getExternalId() : null);
        orderRequest.setSumPaid(productOrder.getSumPaid());
        orderRequest.setLoyaltyCardId(productOrder.getCustomerCard() != null ? productOrder.getCustomerCard().getExternalId() : null);

        if (productOrder.getCustomer() != null) {
            orderRequest.setLastName(productOrder.getCustomer().getLastName());
            orderRequest.setFirstName(productOrder.getCustomer().getFirstName());
            orderRequest.setCustomerId(productOrder.getCustomer().getExternalId());
        }

        if (productOrder.getShippings() != null && productOrder.getShippings().size() > 0) {
            Shipping shipping = productOrder.getShippings().get(0);
            orderRequest.setDeliveryType(shipping.getShippingType() != null ? shipping.getShippingType().getExternalId() : null);
            orderRequest.setDeliveryDate(shipping.getPlanDate());
            orderRequest.setDeliveryCost(BigDecimal.ZERO);
            if (cancelReservation == null) {
                orderRequest.setDeliveryCost(shipping.getApiPrice());
            }
            orderRequest.setDeliveryRecipient(shipping.getRecipientName());
            orderRequest.setDeliveryTimeFrom(shipping.getDeliveryTimeFrom());
            orderRequest.setDeliveryTimeTill(shipping.getDeliveryTimeTill());

            if (shipping.getAddress() != null) {
                Address address = shipping.getAddress();
                DeliveryAddressRequest deliveryAddressRequest = new DeliveryAddressRequest();
                deliveryAddressRequest.setBuilding(address.getBuilding());
                deliveryAddressRequest.setCity(address.getCity());
                deliveryAddressRequest.setFlat(address.getFlat());
                deliveryAddressRequest.setPickupShop(PickupPointId.pickupShopId(productOrder.getShop().getAccount().getPickupPoint(), shipping));
                deliveryAddressRequest.setMetro(address.getMetro());
                deliveryAddressRequest.setStreet(address.getStreet());
                deliveryAddressRequest.setText(address.getStrAddress());
                deliveryAddressRequest.setHousing(address.getHousing());
                deliveryAddressRequest.setZip(address.getZip());
                deliveryAddressRequest.setRegionId(address.getRegionId());
                deliveryAddressRequest.setRegion(address.getRegion());
                orderRequest.setDeliveryAddress(deliveryAddressRequest);
            }
        }

        if (productOrder.getProductOrderItems() != null && cancelReservation == null) {
            orderRequest.setItems(new ArrayList<>());
            productOrder.getProductOrderItems().stream().filter(i -> !i.getDeleted()).forEach(orderItem -> {
                OrderItemRequest orderItemRequest = new OrderItemRequest();
                orderItemRequest.setQuantity(orderItem.getQuantity());
                orderItemRequest.setPrice(orderItem.getItemPrice());
                orderItemRequest.setDiscount(orderItem.getDiscount());
                orderItemRequest.setEstimatedWeight(orderItem.getEstimatedWeight());
                orderItemRequest.setFixedWeight(orderItem.getFixedWeight());
                orderItemRequest.setProductId(orderItem.getProduct().getExternalId());

                if (orderItem.getAttributeValues() != null) {
                    List<CustomAttributeRequest> customAttrRequestList = createCustomAttribute(orderItem.getAttributeValues());
                    orderItemRequest.setCustomAttributes(customAttrRequestList.size() > 0 ? customAttrRequestList : null);
                }

                orderRequest.getItems().add(orderItemRequest);
            });
        }

        if (productOrder.getPersonalDiscounts() != null) {
            List<PersonalDiscountRequest> personalDiscountRequestList = new ArrayList<>();
            productOrder.getPersonalDiscounts().forEach(personalDiscount -> {
                PersonalDiscountRequest personalDiscountRequest = new PersonalDiscountRequest();
                personalDiscountRequest.setCode(personalDiscount.getCode());
                personalDiscountRequest.setId(personalDiscount.getExternalId());
                personalDiscountRequestList.add(personalDiscountRequest);
            });
            orderRequest.setPersonalDiscounts(personalDiscountRequestList);
        }

        if (productOrder.getAttributeValues() != null) {
            List<CustomAttributeRequest> customOrderAttrRequest = createCustomAttribute(productOrder.getAttributeValues());
            orderRequest.setCustomAttributes(customOrderAttrRequest.size() > 0 ? customOrderAttrRequest : null);
        }

        httpClient.postRequest(integrationMicroservice.getUri(), orderRequest);

        log.info("order [externalId = {}] exported", productOrder.getExternalId());
    }

    private <AV extends AbstractAttributeValue<A, T>, A extends AbstractAttribute, T> List<CustomAttributeRequest> createCustomAttribute(List<AV> attributeList) {
        List<CustomAttributeRequest> customAttributeRequestList = new ArrayList<>();

        attributeList.forEach(attributeValue -> {
            CustomAttributeRequest customAttributeRequest = new CustomAttributeRequest();
            customAttributeRequest.setName(attributeValue.getAttribute().getCode());
            customAttributeRequest.setValue(attributeValue.getValue());
            customAttributeRequestList.add(customAttributeRequest);
        });

        return customAttributeRequestList;
    }
}
