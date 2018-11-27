package ru.starfish24.mascotte.service.shipping;

import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.shipping.ShippingRepository;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Shipping;

import java.sql.Date;
import java.util.List;

@Service
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    ShippingRepository shippingRepository;

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Override
    public void updateReservationInfoByOrderId(InternalReservationDto dto, Long orderId) {
        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        List<Shipping> shippingList = productOrder.getShippings();

        if (shippingList != null && !shippingList.isEmpty()) {
            Shipping shipping = shippingList.get(0);
            shipping.setReservationDate(Date.valueOf(dto.getCreateDate().toLocalDate()));
            shippingRepository.save(shipping);
        }
    }
}
