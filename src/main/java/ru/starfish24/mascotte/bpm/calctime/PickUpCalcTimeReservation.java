package ru.starfish24.mascotte.bpm.calctime;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.shipping.ShippingRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Shipping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class PickUpCalcTimeReservation implements JavaDelegate {

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Autowired
    ShippingRepository shippingRepository;

    @Autowired
    InternalReservationControllerApi internalReservationControllerApi;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = Long.parseLong(execution.getVariable(ProcessVar.ORDER_ID).toString());

        Integer dayCount = Integer.parseInt(execution.getVariable("dayCount").toString());

        String deliveryType = (String) execution.getVariable("deliveryType");
        Objects.requireNonNull(deliveryType, "message");

        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        List<Shipping> shippingList = productOrder.getShippings();

        List<InternalReservationDto> reservationDtoList = internalReservationControllerApi.findByOrderIdUsingGET(orderId);

        Date deliveryDate;

        List<Date> dateList = new ArrayList<>();

        if (deliveryType.equalsIgnoreCase("pickup")) {

            reservationDtoList.forEach(x -> {
                        if (x.getReserveWarehouse().getId() == x.getTargetWarehouse().getId()) {
                            dateList.add(addDays(Date.from(x.getCreateDate().atZoneSameInstant(ZoneId.systemDefault()).toInstant()), 1));
                        } else {
                            dateList.add(addDays(Date.from(x.getCreateDate().atZoneSameInstant(ZoneId.systemDefault()).toInstant()), 2));
                        }
                    }
            );
            shippingList.forEach(x -> {
                        x.setReleaseDate((dateList.stream().max(Comparator.comparing(Date::getDate)).get()));//проставить максимальную дату
                        shippingRepository.save(x);
                    }
            );
        } else {

            reservationDtoList.forEach(x -> {
                        if (x.getTargetWarehouse().isPointOfSale() == null || !x.getTargetWarehouse().isPointOfSale()) {
                            dateList.add(addDays(Date.from(x.getCreateDate().atZoneSameInstant(ZoneId.systemDefault()).toInstant()), 1));
                        } else {
                            dateList.add(addDays(Date.from(x.getCreateDate().atZoneSameInstant(ZoneId.systemDefault()).toInstant()), 2));
                        }
                    }
            );

            shippingList.forEach(x -> {
                        x.setDeliveryDate((dateList.stream().max(Comparator.comparing(Date::getDate)).get()));//проставить максимальную дату
                        shippingRepository.save(x);
                    }
            );
        }

    }


    private static Date addDays(Date d, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    private Integer isBankHoliday(java.util.Date d) {

        //List<String> bankHolidays = new ArrayList<>();

        Calendar c = new GregorianCalendar();
        c.setTime(d);
        if ((Calendar.SATURDAY == c.get(c.DAY_OF_WEEK)) /*|| bankHolidays.contains(dString)*/) {
            return 2;
        }
        if (Calendar.SUNDAY == c.get(c.DAY_OF_WEEK)) {
            return 1;
        }
        return 0;
    }
}
