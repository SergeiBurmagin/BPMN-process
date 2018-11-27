package ru.starfish24.mascotte.bpm.orderconfirmation;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.*;
import ru.starfish24.mascotte.bpm.exportreservation.ChangeReservationActivity;

import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Deployment(resources = {"processes/order-confirmation-process.bpmn"})
@RunWith(MockitoJUnitRunner.class)
public class OrderConfirmationProcessTest extends ProcessEngineTestCase {

    private static final String ORDER_CONFIRMATION = "CC01";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario orderConfirmationProcess; // order-confirmation-process.bpmn

    @Mock
    private ChangeOrderStatusActivity changeOrderStatusActivity;

    @Mock
    private StartOrderConfirmationListener startOrderConfirmationListener;

    @Mock
    private CustomerSmsActivity customerSmsActivity;

    @Mock
    private ChangeReservationActivity changeReservationActivity;

    @Mock
    private KpiActivity kpiActivity;

    @Mock
    private AssignmentListener assignmentListener;

    @Mock
    private VerificationActivity verificationActivity;

    @Mock
    private OpenFormListener openFormListener;

    @Before
    public void setup() {
        init(processEngineRule.getProcessEngine());
        Mocks.reset();
    }

    @Test
    public void shouldConfirmNewOrderFromUI() throws Exception {
        Mocks.register("changeOrderStatusActivity", changeOrderStatusActivity);
        Mocks.register("startOrderConfirmationListener", startOrderConfirmationListener);
        Mocks.register("customerSmsActivity", customerSmsActivity);
        Mocks.register("verificationActivity", verificationActivity);

        Map<String, Object> variables = Variables.createVariables();
        variables.put("source", "UI");
        variables.put("statusCode", "new");

        Scenario.run(orderConfirmationProcess).startByKey(ORDER_CONFIRMATION, variables).execute();
        verify(changeOrderStatusActivity, times(1)).execute(any());
        verify(orderConfirmationProcess).hasFinished("orderConfirmationEndEvent");
    }

    @Test
    public void shouldConfirmNewOrderFromOtherSource() throws Exception {
        Mocks.register("changeOrderStatusActivity", changeOrderStatusActivity);
        Mocks.register("startOrderConfirmationListener", startOrderConfirmationListener);
        Mocks.register("customerSmsActivity", customerSmsActivity);
        Mocks.register("verificationActivity", verificationActivity);

        Map<String, Object> variables = Variables.createVariables();
        variables.put("source", "PHONE");
        variables.put("statusCode", "new");
        variables.put("deliveryType", "pickupinstore");

        Scenario.run(orderConfirmationProcess).startByKey(ORDER_CONFIRMATION, variables).execute();
        verify(changeOrderStatusActivity, times(1)).execute(any());
        verify(orderConfirmationProcess).hasFinished("orderConfirmationEndEvent");
    }

    @Test
    public void shouldSuccessConfirmNewOrderFromOtherSource() throws Exception {
        Mocks.register("changeOrderStatusActivity", changeOrderStatusActivity);
        Mocks.register("startOrderConfirmationListener", startOrderConfirmationListener);
        Mocks.register("customerSmsActivity", customerSmsActivity);
        Mocks.register("changeReservationActivity", changeReservationActivity);
        Mocks.register("kpiActivity", kpiActivity);
        Mocks.register("assignmentListener", assignmentListener);
        Mocks.register("openFormListener", openFormListener);
        Mocks.register("verificationActivity", verificationActivity);
        Mocks.register("login", "1");

        Map<String, Object> variables = Variables.createVariables();
        variables.put("source", "API");
        variables.put("statusCode", "new");
        variables.put("deliveryType", "notpickupinstore");
        variables.put("callCounter", 0);

        when(orderConfirmationProcess.waitsAtUserTask("callCustomer"))
                .thenReturn(task -> task.complete(Variables.createVariables().putValue("phoned", true)));
        when(orderConfirmationProcess.waitsAtUserTask("callCustomer_step2"))
                .thenReturn(task -> task.complete(Variables.createVariables().putValue("confirmed", true)));

        Scenario.run(orderConfirmationProcess).startByKey(ORDER_CONFIRMATION, variables).execute();
        verify(changeOrderStatusActivity, times(1)).execute(any());
        verify(kpiActivity, times(1)).execute(any());
        verify(orderConfirmationProcess).hasFinished("orderConfirmationEndEvent");
    }
}