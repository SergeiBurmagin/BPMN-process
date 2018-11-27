package ru.starfish24.mascotte.bpm.orderchange;


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
import ru.starfish24.mascotte.bpm.executereservations.*;
import ru.starfish24.mascotte.bpm.exportreservation.*;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.mockito.Mockito.when;

@Deployment(resources = {"processes/order-change-process.bpmn", "processes/execute-order-reservations.bpmn"})
@RunWith(MockitoJUnitRunner.class)
public class OrderChangeProcessTest extends ProcessEngineTestCase {

    private static final String ORDER_CHANGE = "order-change";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario orderChangeProcess; // order-change-process.bpmn

    @Mock
    private ProcessScenario executeOrderReservations; // processes/execute-order-reservations.bpmn

    @Mock
    private ChangeReservationActivity changeReservationActivity;

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
    private CheckItemStatusActivity checkItemStatusActivity;

    @Mock
    private FindReservationsToExecute findReservationsToExecute;
    @Mock
    private UpdateManualReservationsActivities updateManualReservationsActivities;
    @Mock
    private ExecuteTransfer executeTransfer;
    @Mock
    private ExecuteCancelReservation executeCancelReservation;
    @Mock
    private ExecuteReservations executeReservations;
    @Mock
    private UiRefreshListener uiRefreshListener;

    @Before
    public void setup() {
        init(processEngineRule.getProcessEngine());
        Mocks.reset();
    }

    @Test
    public void shouldChangeOrder() throws Exception {
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

        Mocks.register("uiRefreshListener", uiRefreshListener);

        Map<String, Object> variables = Variables.createVariables();
        variables.put(ProcessVar.ORDER_ID, 1L);
        variables.put("toReserve", new HashMap<Long, Double>(Collections.singletonMap(1L, 4D)));
        variables.put("toCancelReserve", new HashMap<Long, Double>(Collections.singletonMap(1L, 4D)));
        variables.put(ProcessVar.ORDER_ID, 1L);
        when(orderChangeProcess.runsCallActivity("Task_1nupd1z")).thenReturn(
                Scenario.use(executeOrderReservations)
        );
        Scenario.run(orderChangeProcess).startByKey(ORDER_CHANGE, variables).execute();
        //verify(changeReservationActivity, times(1)).execute(any());
        //verify(exportOrder2ErpActivity, times(1)).execute(any());
    }
}
