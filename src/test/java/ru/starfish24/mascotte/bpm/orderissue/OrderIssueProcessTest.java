package ru.starfish24.mascotte.bpm.orderissue;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.*;
import ru.starfish24.mascotte.bpm.cancelreservation.SetupCancelOrderActivity;
import ru.starfish24.mascotte.bpm.executereservations.*;
import ru.starfish24.mascotte.bpm.exportreservation.*;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Deployment(resources = {"processes/order-issue-process.bpmn", "processes/cancel-reservation-process.bpmn", "processes/execute-order-reservations.bpmn"})
@RunWith(MockitoJUnitRunner.class)
public class OrderIssueProcessTest {

    private static final String ORDER_ISSUE = "CC02";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario orderIssueProcess; // order-issue-process.bpmn

    @Mock
    private ProcessScenario cancelReservationProcess; // cancel-reservation-process.bpmn

    @Mock
    private ProcessScenario executeOrderReservations; // execute-order-reservations.bpmn

    @Mock
    private ExportOrder2ErpActivity exportOrder2ErpActivity;

    @Mock
    private CalculateReservationRoadmapActivity calculateReservationRoadmapActivity;

    @Mock
    private SetupReservationDataToAddingActivity setupReservationDataToAddingActivity;

    @Mock
    private ApplyTransitingOrReservationActivity applyTransitingOrReservationActivity;

    @Mock
    private SetupCancelReservationActivity setupCancelReservationActivity;

    @Mock
    private CancelReservationActivity cancelReservationActivity;

    @Mock
    private ChangeOrderStatusActivity changeOrderStatusActivity;

    @Mock
    private CustomerSmsActivity customerSmsActivity;

    @Mock
    private AssignmentListener assignmentListener;

    @Mock
    private OpenFormListener openFormListener;

    @Mock
    private CancelOrderActivity cancelOrderActivity;

    @Mock
    private SetupCancelOrderActivity setupCancelOrderActivity;

    @Mock
    private FindReservationsToExecute findReservationsToExecute;
    @Mock
    private UpdateManualReservationsActivities updateManualReservationsActivities;
    @Mock
    private ExecuteTransfer executeTransfer;
    @Mock
    private ExecuteReservations executeReservations;
    @Mock
    private ExecuteCancelReservation executeCancelReservation;
    @Mock
    private CheckItemStatusActivity checkItemStatusActivity;
    @Mock
    private ChangeReservationActivity changeReservationActivity;


    @Test
    public void shouldNotConfirmedChanges() throws Exception {
        Mocks.register("changeOrderStatusActivity", changeOrderStatusActivity);
        Mocks.register("customerSmsActivity", customerSmsActivity);
        Mocks.register("assignmentListener", assignmentListener);
        Mocks.register("openFormListener", openFormListener);
        Mocks.register("employeeId", "99");
        Mocks.register("cancelOrderActivity", cancelOrderActivity);
        Mocks.register("login", "user");
        Mocks.register("setupCancelOrderActivity", setupCancelOrderActivity);
        Mocks.register("executeOrderReservations", executeOrderReservations);

        Mocks.register("exportOrder2ErpActivity", exportOrder2ErpActivity);
        Mocks.register("changeReservationActivity", changeReservationActivity);
        Mocks.register("calculateReservationRoadmapActivity", calculateReservationRoadmapActivity);
        Mocks.register("setupReservationDataToAddingActivity", setupReservationDataToAddingActivity);
        Mocks.register("applyTransitingOrReservationActivity", applyTransitingOrReservationActivity);
        Mocks.register("setupCancelReservationActivity", setupCancelReservationActivity);
        Mocks.register("cancelReservationActivity", cancelReservationActivity);


        Mocks.register("findReservationsToExecute", findReservationsToExecute);
        Mocks.register("updateManualReservationsActivities", updateManualReservationsActivities);
        Mocks.register("executeTransfer", executeTransfer);
        Mocks.register("executeCancelReservation", executeCancelReservation);
        Mocks.register("executeReservations", executeReservations);

        Mocks.register("checkItemStatusActivity", checkItemStatusActivity);

        when(orderIssueProcess.waitsAtUserTask("orderTrouble_step1")).thenReturn(task -> task.complete());
        when(orderIssueProcess.waitsAtUserTask("orderTrouble_step2"))
                .thenReturn(task -> task.complete(Variables.createVariables().putValue("orderTroubleConfirmed", false)));
        when(orderIssueProcess.runsCallActivity("cancelReservations")).thenReturn(Scenario.use(cancelReservationProcess));
        when(cancelReservationProcess.runsCallActivity("executeOrderReservations")).thenReturn(Scenario.use(executeOrderReservations));
        when(orderIssueProcess.runsCallActivity("executeOrderReservations")).thenReturn(Scenario.use(executeOrderReservations));

        Map<String, Object> variables = Variables.createVariables();
        variables.put(ProcessVar.ORDER_ID, 1L);
        Scenario.run(orderIssueProcess).startByKey(ORDER_ISSUE, variables).execute();
        verify(assignmentListener, times(1)).notify(any());
        verify(openFormListener, times(1)).notify(any());
        verify(cancelOrderActivity, times(1)).execute(any());
    }
}
