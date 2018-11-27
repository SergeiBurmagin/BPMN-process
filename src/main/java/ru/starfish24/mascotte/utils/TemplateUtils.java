package ru.starfish24.mascotte.utils;

import ru.starfish24.starfish24model.Customer;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.ProductOrderItem;
import ru.starfish24.starfish24model.Shipping;
import ru.starfish24.starfish24model.attributes.orederitem.OrderItemAttributeValue;
import ru.starfish24.starfish24model.product.Product;
import ru.starfish24.starfish24model.product.ProductItemType;
import ru.starfish24.starfish24model.product.ProductItemTypeConstants;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateUtils {

    private static final String MULTIPLEEND = "ах";
    private static final String SINGLEEND = "e";
    public static final String COMMA_SPACE = ", ";
    public static final String HOUSE_PREFIX = "д.";
    public static final String STROEN_KORP_PREFIX = "cтроен. / корп.";
    public static final String SPACE = " ";
    public static final String FLAT_PREFIX = "кв.";

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    /*
    {CUSTOMERNAME} - имя покупателя
    {ORDERNUMBER} - номер заказа
    {TOTALSUM} - сумма заказа
    {DELIVERYTIME} - дата и время доставки
    {EAH:DEPARTMENTS} - "е" или "ах"
    {DEPARTMENTS} - список отделов


    {ITEMS}
    URL фото {ITEMIMAGE}
    Название {ITEMNAME}
    Количество/вес {QUANTITY}
    Цена за шт. {NORMALPRICE}
    Цена за позицию в заказе {ITEMPRICE}
    {/ITEMS}
    * */
    public static String fillTemplate(String template, ProductOrder productOrder, String departments) {

        template = template.replaceAll("\\{CUSTOMERNAME\\}", customerName(productOrder));
        template = template.replaceAll("\\{ORDERNUMBER\\}", productOrder.getExternalId());
        template = template.replaceAll("\\{CUSTOMERCOMMENT\\}", productOrder.getCustomerComment());
        if (productOrder.getMainPhone() != null) {
            template = template.replaceAll("\\{PHONE\\}", productOrder.getMainPhone().getPhone());
        }

        template = template.replaceAll("\\{PICKUPSHOP\\}", productOrder.getShop().getTitle());

        if (template.contains("{ITEMS}")) {
            StringBuffer templateBuffer = new StringBuffer(template);

            String itemTemplate = template.substring(template.indexOf("{ITEMS}") + 7, template.indexOf("{/ITEMS}"));
            String filledItemTemplate = productOrder.getProductOrderItems().stream().map(item -> fillItem(itemTemplate, item))
                    .collect(Collectors.joining("\n"));

            templateBuffer.replace(template.indexOf("{ITEMS}"), template.indexOf("{/ITEMS}") + 8, filledItemTemplate);
            template = templateBuffer.toString();
        }

        if (Utils.isNotEmpty(productOrder.getShippings())) {
            Shipping shipping = productOrder.getShippings().get(0);
            if (shipping.getPlanDate() != null) {
                template = template.replaceAll("\\{DELIVERYDATE\\}", Utils.ruSimpleDateFormat("dd MMMM (EEEE)").format(shipping.getPlanDate()));
            } else {
                template = template.replaceAll("\\{DELIVERYDATE\\}", "");
            }
            if (shipping.getDeliveryTimeFrom() != null) {
                template = template.replaceAll("\\{DELIVERYTIME\\}", TIME_FORMAT.format(shipping.getDeliveryTimeFrom()));
            } else {
                template = template.replaceAll("\\{DELIVERYTIME\\}", "");
            }
            if (shipping.getAddress() != null) {
                String shippingAddress = StringJoinerEx.create(COMMA_SPACE).setCrossPairSeparator(SPACE).add(shipping.getAddress().getZip())
                        .add(shipping.getAddress().getCity()).add(shipping.getAddress().getStreet()).add(HOUSE_PREFIX, shipping.getAddress().getBuilding())
                        .add(STROEN_KORP_PREFIX, shipping.getAddress().getHousing()).add(FLAT_PREFIX, shipping.getAddress().getFlat()).toString();
                template = template.replaceAll("\\{PICKUPSHOPADDR\\}", shippingAddress);
            }
        }
        template = template.replaceAll("\\{PICKUPSHOP\\}", productOrder.getShop().getTitle());
        template = template.replaceAll("\\{TOTALSUM\\}", Utils.priceFormat().format(productOrder.getTotalPrice()));
        if (productOrder.getEmail() != null) {
            template = template.replaceAll("\\{CUSTOMEREMAIL\\}", productOrder.getEmail().getEmail());
        }
        if (productOrder.getMainPhone() != null) {
            template = template.replaceAll("\\{CUSTOMERPHONE\\}", productOrder.getMainPhone().getPhone());
        }
        if (!Utils.isEmpty(departments)) {
            template = template.replaceAll("\\{EAH:DEPARTMENTS\\}", departments.contains(",") ? MULTIPLEEND : SINGLEEND);
            template = template.replaceAll("\\{DEPARTMENTS\\}", departments);
        } else {
            template = template.replaceAll("\\{EAH:DEPARTMENTS\\}", MULTIPLEEND);
            template = template.replaceAll("\\{DEPARTMENTS\\}", "магазина");
        }

        return template;
    }

    private static String customerName(ProductOrder productOrder) {
        return Optional.ofNullable(productOrder).map(ProductOrder::getCustomer).map(Customer::getFirstName).orElse("");
    }

    public static String fillItem(String template, ProductOrderItem productOrderItem) {
        String imageUrl = productOrderItem.getProduct().getImageUrl();
        String productName = productOrderItem.getProduct().getName();
        template = template.replaceAll("\\{ITEMIMAGE\\}", imageUrl != null ? imageUrl : "");
        template = template.replaceAll("\\{ITEMNAME\\}", productName != null ? productName : "");

        Integer quantity = productOrderItem.getQuantity();
        template = template.replaceAll("\\{QUANTITY\\}", quantity != null ? productOrderItem.getQuantity().toString() : "");

        BigDecimal estimatedWeight = productOrderItem.getEstimatedWeight();
        template = template.replaceAll("\\{WEIGHT\\}", estimatedWeight != null ? estimatedWeight.toString() : "");

        if (productOrderItem.getProduct().getItemType() == ProductItemType.piece) {
            template = template.replaceAll("\\{ITEMTYPE\\}", ProductItemTypeConstants.PIECES_NAME);
        } else {
            template = template.replaceAll("\\{ITEMTYPE\\}", ProductItemTypeConstants.WEIGHT_NAME);
        }

        if (productOrderItem.getPrice() != null) {
            template = template.replaceAll("\\{NORMALPRICE\\}", Utils.priceFormat().format(productOrderItem.getPrice()));
            template = template.replaceAll("\\{ITEMPRICE\\}", Utils.priceFormat().format(orderItemSum(productOrderItem)));
        }

        for (OrderItemAttributeValue attrVal : productOrderItem.getAttributeValues()) {
            template = template.replaceAll(String.format("\\{ATTR:%s\\}", attrVal.getAttribute().getCode()), attrVal.getValue() != null ? attrVal.getValue() : "");
        }

        template = template.replaceAll("\\{ATTR.*?\\}", "");

        return template;
    }


    public static BigDecimal orderItemSum(ProductOrderItem item) {
        if (item == null) return BigDecimal.ZERO;
        Product product = item.getProduct();
        if (product == null) return BigDecimal.ZERO;
        ProductItemType itemType = product.getItemType();
        if (itemType == null) itemType = ProductItemType.piece;

        return itemType.orderItemSum(item);
    }

    public static String fillSubject(String themeTemplate, ProductOrder productOrder) {
        themeTemplate = themeTemplate.replaceAll("\\{CUSTOMERNAME\\}", customerName(productOrder));
        themeTemplate = themeTemplate.replaceAll("\\{ORDERNUMBER\\}", productOrder.getExternalId());

        return themeTemplate;
    }

}
