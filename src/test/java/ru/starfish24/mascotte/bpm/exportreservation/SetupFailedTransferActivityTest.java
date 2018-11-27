package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.starfish24.mascotte.bpm.exportreservation.dto.OrderItemDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetupFailedTransferActivityTest {

    private SetupFailedTransferActivity setupFailedTransferActivity;

    @Mock
    private InternalReservationControllerApi api;

    @Mock
    private ReservationService reservationService;

    @Mock
    private DelegateExecution delegation;

    @Before
    public void setup() {
        setupFailedTransferActivity = new SetupFailedTransferActivity(api, reservationService);
    }

    private PrimitiveIterator.OfInt randomIterator = new Random().ints(0, 5).iterator();

    @Test
    public void execute() throws Exception {

        when(delegation.getVariable("failedToTransfer")).thenReturn(
                Arrays.asList(1L, 2L, 3L)
        );

        when(delegation.getVariable("orderId")).thenReturn(1L);

        when(delegation.getVariable(ProcessVar.RESERVATION_ROADMAP)).thenReturn(
                getReservationRoadMap()
        );

        when(api.getUsingGET(anyLong())).thenReturn(generatedInternalReservationDto());


        when(reservationService.internalReservationCanceled(any())).thenReturn(null);


        setupFailedTransferActivity.execute(delegation);

    }

    private InternalReservationDto generatedInternalReservationDto() {
        InternalReservationDto dto = new InternalReservationDto();
        dto.setOrderItemId(randomIterator.next().longValue());
        return dto;
    }

    private List<ShipToCustomerResultDto> getReservationRoadMap() {
        ShipToCustomerResultDto obj1 = createShipToCustomerResultDto();
        ShipToCustomerResultDto obj2 = createShipToCustomerResultDto();
        ShipToCustomerResultDto obj3 = createShipToCustomerResultDto();
        ShipToCustomerResultDto obj4 = createShipToCustomerResultDto();
        return Arrays.asList(obj1, obj2, obj3, obj4);
    }

    private ShipToCustomerResultDto createShipToCustomerResultDto() {
        ShipToCustomerResultDto obj = new ShipToCustomerResultDto();
        OrderItemDto item = new OrderItemDto();
        item.setId(111L);
        item.setProductId(randomIterator.next().longValue());
        item.setQuantity(randomIterator.next());
        obj.setProductOrderItem(item);
        return obj;
    }
}