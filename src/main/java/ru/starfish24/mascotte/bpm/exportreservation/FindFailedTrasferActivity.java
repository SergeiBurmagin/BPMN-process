package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ищет резервы по которым не удалось сделать перемещение. и помещает их в переменную БП failedToTransfer
 */
@Service
public class FindFailedTrasferActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;

    public FindFailedTrasferActivity(InternalReservationControllerApi internalReservationControllerApi) {
        this.internalReservationControllerApi = internalReservationControllerApi;
    }


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = new Long(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        List<InternalReservationDto> reservations = internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        List<Long> failedTrasfer = reservations.stream()
                .filter(r -> r.getNextReservation() == null)
                .filter(r -> r.getStatus().equals(InternalReservationDto.StatusEnum.DO_RESERVE))
                .filter(r -> r.getTransferStatus().equals(InternalReservationDto.TransferStatusEnum.CANCELED)).map(r -> r.getId()).collect(Collectors.toList());
        delegateExecution.setVariable("failedToTransfer", failedTrasfer);
    }
}
