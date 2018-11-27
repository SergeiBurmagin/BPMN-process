package ru.starfish24.mascotte.service.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.*;
import java.util.stream.Collectors;

@Service("orderService")
@Slf4j
public class OrderService {

    private final ProductOrderRepository productOrderRepository;

    private final HttpClient httpClient;

    public OrderService(ProductOrderRepository productOrderRepository, HttpClient httpClient) {
        this.productOrderRepository = productOrderRepository;
        this.httpClient = httpClient;
    }

    /**
     * Ищет заказы со статусом @statuses и старше  @daysOld
     *
     * @param statuses
     * @param daysOld
     * @return
     */
    public Set<ProductOrder> findOrdersByStatusesAndDaysOld(String accountType, Set<String> statuses, int daysOld) {
        log.debug("Find orders with status = {}", statuses.stream().collect(Collectors.joining(",")));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysOld);
        return new HashSet<>(productOrderRepository.findAllByStatusAndDaysOld(accountType, statuses, calendar.getTime()));
    }

    public boolean startUpdateStatuses(Set<ProductOrder> productOrders) {
        final List<ProductOrder> batched = new LinkedList<>();
        Set<String> externalIdSet = new HashSet<>();
        ProductOrder productOrder = productOrders.stream().findFirst().orElse(null);
        if (productOrder != null) {
            Long accountId = productOrder.getShop().getAccount().getId();
            productOrders.stream().filter(order -> order.getExternalId() != null).forEach(order -> {
                externalIdSet.add(order.getExternalId());
                batched.add(order);
            });

            try {
                httpClient.postRequest(accountId, "itemstatus_batch", externalIdSet.toArray());
            } catch (Exception ex) {
                log.error("Failed to update order status batch {}", ex);
            }

            log.info("Total orders to update [count = {}]", productOrders.size());
            log.info("Total batch size update orders [count = {}]", batched.size());
            return true;
        }
        return false;
    }
}
