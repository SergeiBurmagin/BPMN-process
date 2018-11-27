package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
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
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindFailedTrasferActivityTest {

    public static final Long ID = 112L;

    private FindFailedTrasferActivity activity;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private InternalReservationControllerApi internalReservationControllerApi;

    @Captor
    private ArgumentCaptor<List<Long>> resultCaptor;

    @Before
    public void setup() {
        activity = new FindFailedTrasferActivity(internalReservationControllerApi);
    }


    @Test
    public void execute() throws Exception {
        List<InternalReservationDto> reserves = createReservers();
        Long orderId = 1L;
        when(delegateExecution.getVariableTyped(ProcessVar.ORDER_ID)).thenReturn(Variables.integerValue(orderId.intValue()));
        when(internalReservationControllerApi.findByOrderIdUsingGET(orderId)).thenReturn(reserves);
        activity.execute(delegateExecution);
        verify(delegateExecution).setVariable(eq("failedToTransfer"), resultCaptor.capture());
        assertEquals(1, resultCaptor.getValue().size());
        assertEquals(ID, resultCaptor.getValue().get(0));
    }

    private List<InternalReservationDto> createReservers() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setTransferStatus(InternalReservationDto.TransferStatusEnum.CANCELED);
        dto.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);
        dto.setId(ID);

        InternalReservationDto dto1 = new InternalReservationDto();
        dto1.setTransferStatus(InternalReservationDto.TransferStatusEnum.CANCELED);
        dto1.setStatus(InternalReservationDto.StatusEnum.CANCEL_RESERVE);

        InternalReservationDto dto2 = new InternalReservationDto();
        dto2.setTransferStatus(InternalReservationDto.TransferStatusEnum.CANCELED);
        dto2.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);
        dto2.setNextReservation(new InternalReservationDto());
        List<InternalReservationDto> internalReservationDtos = new ArrayList<>();
        internalReservationDtos.add(dto);
        internalReservationDtos.add(dto1);
        internalReservationDtos.add(dto2);
        return internalReservationDtos;

    }
}