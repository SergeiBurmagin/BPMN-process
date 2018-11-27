package ru.starfish24.mascotte.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.order.OrderService;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderStatusUpdateJob {

    private final ProductOrderRepository productOrderRepository;

    private final OrderService orderService;
    @Value("${starfish24.job.status.update.days}")
    private String daysAmount;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statusUpdate1() {
        Set<String> statuses = new HashSet();
        statuses.add("new");
        statuses.add("confirmed");
        Integer amount = Integer.parseInt(daysAmount);
        log.info("Find orders with status = {}", statuses.stream().collect(Collectors.joining(",")));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(amount));
        Set<ProductOrder> orders = new HashSet<>(productOrderRepository.findAllByStatusAndDaysOld("mascotte", statuses, calendar.getTime()));
        orderService.startUpdateStatuses(orders);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void statusUpdate2() {
        Set<String> statuses = new HashSet();
        statuses.add("readyforrelease");
        statuses.add("trouble");
        statuses.add("salon");
        statuses.add("tk");
        statuses.add("picked");
        Integer amount = Integer.parseInt(daysAmount);
        log.info("Find orders with status = {}", statuses.stream().collect(Collectors.joining(",")));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(amount));
        Set<ProductOrder> orders = new HashSet<>(productOrderRepository.findAllByStatusAndDaysOld("mascotte", statuses, calendar.getTime()));
        orderService.startUpdateStatuses(orders);
    }
}
