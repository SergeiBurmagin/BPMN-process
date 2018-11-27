package ru.starfish24.mascotte.dto.exportorder;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderRequest {

    private String shop;

    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timeCreated;

    private BigDecimal discount;

    private Integer discountType;

    private String lastName;

    private String firstName;

    private String phone;

    private String additionalPhone;

    private String email;

    private Boolean needCall;

    private Boolean needSms;

    private String customerComment;

    private String customerId;

    private String paymentType;

    private Boolean isCanceled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timeCanceled;

    private String reasonCanceled;

    private Boolean isPaid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timePaid;

    private String status;

    private List<OrderItemRequest> items;

    private String deliveryType;

    private String deliveryRecipient;

    private BigDecimal deliveryCost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date deliveryDate;

    private DeliveryAddressRequest deliveryAddress;

    private BigDecimal sumPaid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTimeFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTimeTill;

    private List<CustomAttributeRequest> customAttributes;

    private String loyaltyCardId;

    private List<PersonalDiscountRequest> personalDiscounts;


}
