package ru.starfish24.mascotte.bpm.executereservations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.WarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.service.reservation.ExecuteReservationService;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Устанавливает целевой склад и перемещения для резервов которым склад резерва был назначен вручную.
 */
@Service
@Slf4j
public class UpdateManualReservationsActivities implements JavaDelegate {

    private final ExecuteReservationService executeReservationService;

    private final ReservationService reservationService;

    private final IntegrationMicroserviceRepository integrationMicroserviceRepository;

    private final ProductOrderRepository productOrderRepository;

    private final HttpClient httpClient;

    private final ObjectMapper mapper;

    @Autowired
    public UpdateManualReservationsActivities(ExecuteReservationService executeReservationService, ReservationService reservationService, IntegrationMicroserviceRepository integrationMicroserviceRepository, ProductOrderRepository productOrderRepository, HttpClient httpClient, ObjectMapper mapper) {
        this.executeReservationService = executeReservationService;
        this.reservationService = reservationService;
        this.integrationMicroserviceRepository = integrationMicroserviceRepository;
        this.productOrderRepository = productOrderRepository;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long orderId = Long.valueOf(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
        Map<Long, String> toExecute = (Map<Long, String>) delegateExecution.getVariable("toExecute");
        log.info("start UpdateManualReservationsActivities: {}", toExecute);
        List<InternalReservationDto> dtos = toExecute.entrySet().stream().map(r -> executeReservationService.toDto(r.getValue())).collect(Collectors.toList());
        dtos.stream().filter(this::findCondition)
                .forEach(it -> enreach(toExecute, orderId, it));
        delegateExecution.setVariable("toExecute", toExecute);
    }

    private boolean findCondition(InternalReservationDto dto) {
        if (dto.getStatus() != null
                && dto.getStatus().equals(InternalReservationDto.StatusEnum.MANUAL_RESERVE_WAREHOUSE)) {
            if (dto.getReserveWarehouse() != null && dto.getReserveWarehouse().getId() != null) {
                return true;
            }
        }
        return false;
    }

    private void enreach(Map<Long, String> toExecute, Long orderId, InternalReservationDto reservation) {
        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(productOrder.getShop().getAccount().getId(), "orderBroker");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'orderBroker', accountId = {}] NOT FOUND", productOrder.getShop().getAccount().getId());
            return;
        }
        Map<String, String> params = new HashMap<>(1);
        params.put("orderId", productOrder.getExternalId());
        params.put("orderItemId", reservation.getOrderItemId().toString());
        params.put("reserveWarehouseId", reservation.getReserveWarehouse().getId().toString());
        try {
            String json = httpClient.getRequest(integrationMicroservice.getUri(), params);
            List<ShipToCustomerResultDto> roadmap =
                    mapper.readValue(json, new TypeReference<List<ShipToCustomerResultDto>>() {
                    });
            if (roadmap.size() > 0) {
                ShipToCustomerResultDto road = roadmap.get(0);
                reservation.setTransitWarehouse(convert(road.getTransit()));
                reservation.setTargetWarehouse(convert(road.getTarget()));
                if (road.getTransition() != null) {
                    reservation.setTransferWarehouseTo(convert(road.getTransition().getTo()));
                    reservation.setTransferWarehouseFrom(convert(road.getTransition().getFrom()));
                    reservation.setTransferStatus(InternalReservationDto.TransferStatusEnum.TRANSFER);
                } else {
                    reservation.setTransferStatus(InternalReservationDto.TransferStatusEnum.NO_TRANSFER);
                }
                reservation.setStatus(InternalReservationDto.StatusEnum.DO_RESERVE);
                toExecute.put(reservation.getId(), executeReservationService.toString(reservation));
            } else {
                log.info("Failed to obtain reserve information for order. [externalId={}]", productOrder.getExternalId());
            }
        } catch (IOException e) {
            throw new RuntimeException("UpdateManualReserv is handle to exception", e);
        }
    }

    private WarehouseDto convert(ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto from) {
        WarehouseDto result = new WarehouseDto();
        if (from != null) {
            result.setId(reservationService.getWarehouseId(from));
            result.setExternalId(from.getExternalId());
        }
        return result;
    }
}
