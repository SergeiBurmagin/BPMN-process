package ru.starfish24.mascotte.bpm.exportreservation;

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
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.LinkedList;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Deployment(resources = {"processes/export-reservation-process.bpmn"})
@RunWith(MockitoJUnitRunner.class)
public class ExportReservationProcessTest extends ProcessEngineTestCase {

    private static final String EXPORT_RESERVATION = "RS01";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private ProcessScenario exportReservationProcess; // export-reservation-process.bpmn

    @Mock
    private ExportOrder2ErpActivity exportOrder2ErpActivity;

    @Mock
    private CalculateReservationRoadmapActivity calculateReservationRoadmapActivity;

    @Mock
    private ReservationActivity reservationActivity;

    @Mock
    private CheckItemStatusActivity checkItemStatusActivity;

    @Mock
    private LoyaltyCardAsyncTask loyaltyCardAsyncTask;

    @Mock
    private FindFailedTrasferActivity findFailedTrasferActivity;

    @Mock
    private ProcessFailedTransferActivity processFailedTransferActivity;

    @Mock
    private SetupFailedTransferActivity setupFailedTransferActivity;

    @Before
    public void setup() {
        init(processEngineRule.getProcessEngine());
        Mocks.reset();
    }

    @Test
    public void testExportReservationProcess() {
        Mocks.register("exportOrder2ErpActivity", exportOrder2ErpActivity);
        Mocks.register("calculateReservationRoadmapActivity", calculateReservationRoadmapActivity);
        Mocks.register("reservationActivity", reservationActivity);
        Mocks.register("checkItemStatusActivity", checkItemStatusActivity);
        Mocks.register("findFailedTrasferActivity", findFailedTrasferActivity);
        Mocks.register("processFailedTransferActivity", processFailedTransferActivity);
        Mocks.register("setupFailedTransferActivity", setupFailedTransferActivity);
        Mocks.register("loyaltyCardAsyncTask", loyaltyCardAsyncTask);

        Map<String, Object> variables = Variables.createVariables();
        variables.put(ProcessVar.ORDER_ID, 1L);
        variables.put(ProcessVar.ACCOUNT_ID, 1L);
        variables.put(ProcessVar.SHOP_ID, 1L);
        variables.put("failedToTransfer", new LinkedList<Long>());
        when(exportReservationProcess.waitsAtMessageIntermediateThrowEvent("reservationPlacedThrowEvent")).
                thenReturn(signal -> signal.complete());

        Scenario.run(exportReservationProcess).startByKey(EXPORT_RESERVATION, variables).execute();
        verify(exportReservationProcess).hasFinished("exportReservationEndEvent");
    }
}
