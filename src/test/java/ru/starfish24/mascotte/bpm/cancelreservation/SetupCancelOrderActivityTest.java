package ru.starfish24.mascotte.bpm.cancelreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetupCancelOrderActivityTest {

    @Mock
    private InternalReservationControllerApi internalReservationControllerApi;

    private SetupCancelOrderActivity setupCancelOrderActivity;

    @Mock
    private DelegateExecution delegation;

    @Before
    public void setup() {
        setupCancelOrderActivity = new SetupCancelOrderActivity(internalReservationControllerApi);
    }

    @Test
    public void execute() throws Exception {

        Long orderId = 1L;
        when(delegation.getVariable("orderId")).thenReturn(orderId);
        List<InternalReservationDto> reserved = new ArrayList<>();
        Long reserveId1 = 2L;
        Long reserveId2 = 3L;
        reserved.add(createReserve(reserveId1));
        reserved.add(createReserve(reserveId2));
        when(internalReservationControllerApi.getReservedUsingGET(orderId)).thenReturn(reserved);
        setupCancelOrderActivity.execute(delegation);
        verify(internalReservationControllerApi, times(1)).cancelUsingPOST(2L);
        verify(internalReservationControllerApi, times(1)).cancelUsingPOST(3L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoOrderId() throws Exception {
        when(delegation.getVariable("orderId")).thenReturn(null);
        setupCancelOrderActivity.execute(delegation);
    }

    private InternalReservationDto createReserve(Long reserveId) {
        InternalReservationDto internalReservationDto = new InternalReservationDto();
        internalReservationDto.setId(reserveId);
        return internalReservationDto;
    }


}