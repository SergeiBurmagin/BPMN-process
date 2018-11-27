package ru.starfish24.mascotte.bpm.cancelreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetupCancelOrderActivity implements JavaDelegate {

    private final InternalReservationControllerApi internalReservationControllerApi;

    @Autowired
    public SetupCancelOrderActivity(InternalReservationControllerApi internalReservationControllerApi) {
        this.internalReservationControllerApi = internalReservationControllerApi;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Object oid = delegateExecution.getVariable("orderId");
        if (oid == null) {
            throw new IllegalArgumentException("Order ID was not set for the process");
        }
        Long orderId = new Long(delegateExecution.getVariable("orderId").toString());
        List<InternalReservationDto> internalReservationList =
                internalReservationControllerApi.findByOrderIdUsingGET(orderId);
        if (internalReservationList == null) {
            throw new BpmnError("No internal reservation found for order = [" + orderId + "]");
        }
        internalReservationList.stream().filter(it -> it.getNextReservation() == null)
                .forEach(r -> {
                    internalReservationControllerApi.cancelUsingPOST(r.getId());
                });
    }

}
