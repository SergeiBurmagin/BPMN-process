package ru.starfish24.mascotte.bpm.orderstatusupdate;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.service.order.OrderService;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.HashSet;
import java.util.Set;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@Deployment(resources = {"processes/order-status-update.bpmn"})
@RunWith(MockitoJUnitRunner.class)
public class OrderStatusUpdateTest extends ProcessEngineTestCase {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario orderStatusUpdateProcess;

    @Mock
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Set<String>> setArgumentCaptor;

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor;

    @Before
    public void setup() {
        init(processEngineRule.getProcessEngine());
        Mocks.reset();
    }

    @Test
    public void OrderStatusUpdate() {
        Mocks.register("orderService", orderService);
        String accountType = "mascotte";
        Set<ProductOrder> orders = new HashSet<>();
        when(orderService.findOrdersByStatusesAndDaysOld(eq(accountType), any(), anyInt())).thenReturn(orders);
        when(orderService.startUpdateStatuses(eq(orders))).thenReturn(true);
        Scenario.run(orderStatusUpdateProcess).startByKey("order-status-update").execute();
        verify(orderService, times(1)).findOrdersByStatusesAndDaysOld(eq(accountType), setArgumentCaptor.capture(), integerArgumentCaptor.capture());
        verify(orderService, times(1)).startUpdateStatuses(eq(orders));
        assertTrue(setArgumentCaptor.getValue().contains("new"));
        assertTrue(setArgumentCaptor.getValue().contains("confirmed"));
        assertEquals(5, (int) integerArgumentCaptor.getValue());
    }
}
