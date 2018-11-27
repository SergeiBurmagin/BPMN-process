package ru.starfish24.mascotte.bpm.executereservations;

import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.service.reservation.ChangeReservationActivityService;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FindReservationsToExecuteTest {

    @Mock
    private ExecuteReservationService executeReservationService;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private ChangeReservationActivityService changeReservationActivityService;

    @Captor
    private ArgumentCaptor<Map<Long, String>> mapArgumentCaptor;

    private FindReservationsToExecute findReservationsToExecute;


    @Before
    public void setup() {
        findReservationsToExecute = new FindReservationsToExecute(changeReservationActivityService, executeReservationService);
    }


    @Test
    public void execute() throws Exception {
        Long orderId = 1L;
        when(delegateExecution.getVariableTyped("orderId")).thenReturn(Variables.longValue(orderId));
        List<InternalReservationDto> reservations = new ArrayList<>();
        reservations.add(createReservation(101L, InternalReservationDto.StatusEnum.DO_RESERVE));
        reservations.add(createReservation(102L, InternalReservationDto.StatusEnum.RESERVED));
        reservations.add(createReservation(103L, InternalReservationDto.StatusEnum.CANCEL_RESERVE));
        when(changeReservationActivityService.getInternalReservations(orderId)).thenReturn(reservations);
        when(executeReservationService.toString(reservations.get(0))).thenReturn("test1");
        when(executeReservationService.toString(reservations.get(2))).thenReturn("test2");
        doNothing().when(delegateExecution).setVariable(eq("toExecute"), any());
        findReservationsToExecute.execute(delegateExecution);
        verify(delegateExecution, times(1)).setVariable(eq("toExecute"), mapArgumentCaptor.capture());
        assertEquals(2, mapArgumentCaptor.getValue().size());
        assertTrue(mapArgumentCaptor.getValue().containsKey(101L));
        assertTrue(mapArgumentCaptor.getValue().containsKey(103L));
        assertEquals("test1", mapArgumentCaptor.getValue().get(101L));
        assertEquals("test2", mapArgumentCaptor.getValue().get(103L));
    }

    @Test
    public void execute1() throws Exception {
        Long orderId = 1L;
        Integer sampleValue = 1;
        when(delegateExecution.getVariableTyped("orderId")).thenReturn(Variables.integerValue(sampleValue));
        when(delegateExecution.getVariable("orderId")).thenReturn(sampleValue);
        List<InternalReservationDto> reservations = new ArrayList<>();
        reservations.add(createReservation(101L, InternalReservationDto.StatusEnum.DO_RESERVE));
        reservations.add(createReservation(102L, InternalReservationDto.StatusEnum.RESERVED));
        reservations.add(createReservation(103L, InternalReservationDto.StatusEnum.CANCEL_RESERVE));
        when(changeReservationActivityService.getInternalReservations(orderId)).thenReturn(reservations);
        when(executeReservationService.toString(reservations.get(0))).thenReturn("test1");
        when(executeReservationService.toString(reservations.get(2))).thenReturn("test2");
        doNothing().when(delegateExecution).setVariable(eq("toExecute"), any());
        findReservationsToExecute.execute(delegateExecution);
        verify(delegateExecution, times(1)).setVariable(eq("toExecute"), mapArgumentCaptor.capture());
        assertEquals(2, mapArgumentCaptor.getValue().size());
        assertTrue(mapArgumentCaptor.getValue().containsKey(101L));
        assertTrue(mapArgumentCaptor.getValue().containsKey(103L));
        assertEquals("test1", mapArgumentCaptor.getValue().get(101L));
        assertEquals("test2", mapArgumentCaptor.getValue().get(103L));
    }

    private InternalReservationDto createReservation(long id, InternalReservationDto.StatusEnum cancelReserve) {
        InternalReservationDto reservationDto = new InternalReservationDto();
        reservationDto.setId(id);
        reservationDto.status(cancelReserve);
        return reservationDto;
    }
}