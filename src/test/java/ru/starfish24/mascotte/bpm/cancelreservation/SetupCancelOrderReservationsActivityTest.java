package ru.starfish24.mascotte.bpm.cancelreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetupCancelOrderReservationsActivityTest {

    @Mock
    private InternalReservationControllerApi internalReservationControllerApi;

    @Mock
    private DelegateExecution delegation;

    SetupCancelOrderReservationsActivity activity;

    @Before
    public void setup() {
        activity = new SetupCancelOrderReservationsActivity(internalReservationControllerApi);
    }

    @Test
    public void execute() throws Exception {

        Long orderId = 1L;
        List<InternalReservationDto> reservations = new ArrayList<>();
        reservations.add(createReservation(2L, InternalReservationDto.StatusEnum.RESERVED));
        reservations.add(createReservation(3L, InternalReservationDto.StatusEnum.DO_RESERVE));
        reservations.add(createReservation(4L, InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE));
        when(internalReservationControllerApi.findByOrderIdUsingGET(orderId)).thenReturn(reservations);
        when(delegation.getVariable("orderId")).thenReturn(orderId);
        activity.execute(delegation);
        verify(internalReservationControllerApi, times(1)).canceledUsingPOST(4L);
        verify(internalReservationControllerApi, times(1)).canceledUsingPOST(3L);
        verify(internalReservationControllerApi, times(1)).cancelUsingPOST(2L);
    }

    private InternalReservationDto createReservation(long id, InternalReservationDto.StatusEnum statusEnum) {
        InternalReservationDto internalReservationDto = new InternalReservationDto();
        internalReservationDto.setId(id);
        internalReservationDto.setStatus(statusEnum);
        return internalReservationDto;
    }
}