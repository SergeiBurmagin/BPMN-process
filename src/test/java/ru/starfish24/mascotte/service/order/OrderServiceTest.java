package ru.starfish24.mascotte.service.order;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.starfish24model.Account;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Shop;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    private OrderService orderService;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private HttpClient httpClient;

    @Captor
    ArgumentCaptor<Date> dateArgumentCaptor;

    @Captor
    ArgumentCaptor<Set<String>> paramsCaptor;

    @Before
    public void setup() {
        orderService = new OrderService(productOrderRepository, httpClient);
    }

    @Test
    public void findOrdersByStatusesAndDaysOld() {
        Set<String> statuses = new HashSet<>();
        int daysOld = 5;
        List<ProductOrder> orders = new ArrayList<>();
        String accountType = "testAcc";
        when(productOrderRepository.findAllByStatusAndDaysOld(eq(accountType), any(), dateArgumentCaptor.capture())).thenReturn(orders);
        orderService.findOrdersByStatusesAndDaysOld(accountType, statuses, daysOld);
        verify(productOrderRepository, times(1)).findAllByStatusAndDaysOld(eq(accountType), any(), dateArgumentCaptor.capture());
        Calendar resultDate = Calendar.getInstance();
        resultDate.setTime(dateArgumentCaptor.getValue());
        Calendar now = Calendar.getInstance();
        Assert.assertEquals(daysOld, ChronoUnit.DAYS.between(resultDate.toInstant(), now.toInstant()));
    }

    @Test
    public void startUpdateStatuses() {
        when(httpClient.getRequest(eq(1L), eq("itemstatus_batch"), any())).thenReturn("ok");
        boolean result = orderService.startUpdateStatuses(new HashSet<>(createSurrogateList()));
        assertTrue(result);
    }

    private List<ProductOrder> createSurrogateList() {
        return Stream.of(createProductSurrogate(), createProductSurrogate(), createProductSurrogate())
                .collect(Collectors.toList());
    }

    private ProductOrder createProductSurrogate() {
        ProductOrder productOrder = new ProductOrder();
        Shop shop = new Shop();
        Account account = new Account();
        account.setId(new Random(5).nextLong());
        shop.setAccount(account);
        productOrder.setShop(shop);
        productOrder.setExternalId("externalId-" + new Random(4).nextInt());
        return productOrder;
    }
}