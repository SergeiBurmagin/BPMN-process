package ru.starfish24.mascotte.bpm.exportreservation;

import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto;
import ru.starfish24.mascotte.repository.warehouse.WarehouseRepository;
import ru.starfish24.mascotte.service.reservation.ReservationService;
import ru.starfish24.mascotte.utils.ProcessVar;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Подготовить данные для резервации или перемещения
 * на вход приходит Map<Long, Double>, где Long это идентификатор товара а Double это разница
 * между заврезервирвонными позциями заказа, и теми что есть.
 * И RESERVATION_ROADMAP - карта резервирования
 */
@Service
@Slf4j
public class SetupReservationDataToAddingActivity implements JavaDelegate {

    private final ReservationService reservationService;

    private final WarehouseRepository warehouseRepository;

    @Autowired
    public SetupReservationDataToAddingActivity(ReservationService reservationService,
                                                WarehouseRepository warehouseRepository) {
        this.reservationService = reservationService;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        // get variable toReserve
        // get ProcessVar.RESERVATION_ROADMAP Reservation roadmap
        Long orderId = new Long(delegateExecution.getVariable("orderId").toString());
        log.info("Starting SetupReservationDataToAddingActivity# for order = [{}]", orderId);
        //
        Map<Long, Double> toReserve = (Map<Long, Double>) delegateExecution
                .getVariable("toReserve");
        Objects.requireNonNull(toReserve, "toReserve is not be set, stop process");

        if (log.isDebugEnabled()) {
            log.debug("SetupReservationDataToAddingActivity# toReserve Reserve = {}",
                    toReserve.entrySet().stream().map(p -> p.getKey() + "=" + p.getValue())
                            .collect(Collectors.joining(",")));
        }
        List<ShipToCustomerResultDto> roadMap =
                (List<ShipToCustomerResultDto>) delegateExecution.getVariable(ProcessVar.RESERVATION_ROADMAP);

        Assert.notNull(roadMap, "SetupReservationDataToAddingActivity# Reservation map is null");

        if (log.isDebugEnabled()) {
            log.debug("SetupReservationDataToAddingActivity from order-broker = {}", roadMap.stream().map(
                    p -> p != null ? p.toString() : "reservation order item is null"
            ).collect(Collectors.joining(",")));
        }

        // Отфильтруем лишние товары
        roadMap.stream().filter(it -> toReserve.containsKey(it.getProductOrderItem().getProductId()))
                .filter(obj -> validate(obj.getReserve())).forEach(reservationObj -> {
            log.debug(reservationObj.toString());
            Long warehouseReserveId = warehouseRepository
                    .findOneByExternalId(reservationObj.getReserve().getExternalId()).getId();

            Long warehouseTargetId = reservationObj.getTarget() != null ? warehouseRepository
                    .findOneByExternalId(reservationObj.getTarget().getExternalId()).getId() : null;

            Long warehouseTransitId = reservationObj.getTransit() != null ?
                    warehouseRepository.findOneByExternalId(reservationObj.getTransit().getExternalId()).getId()
                    : null;
            Long warehouseTransferFrom = null;
            Long warehouseTransferTo = null;
            if (reservationObj.getTransition() != null) {
                warehouseTransferFrom = warehouseRepository.findOneByExternalId(
                        reservationObj.getTransition().getFrom().getExternalId()
                ).getId();
                warehouseTransferTo = warehouseRepository.findOneByExternalId(
                        reservationObj.getTransition().getTo().getExternalId()
                ).getId();
            }
            Double qty = toReserve.get(reservationObj.getProductOrderItem().getProductId());
            if (qty > 0) {
                InternalReservationDto created = reservationService.createInternalReservationDto(
                        InternalReservationRequest.StatusEnum.DO_RESERVE,
                        qty,
                        reservationObj.getProductOrderItem().getId(),
                        warehouseTargetId,
                        warehouseReserveId,
                        warehouseTransitId,
                        warehouseTransferFrom,
                        warehouseTransferTo);
                log.debug("New reservation object for reservation operation, CREATED = {}", created);
            }
        });
    }

    private boolean validate(WarehouseDto reserve) {
        if (reserve != null) {
            if (reserve.getExternalId() != null)
                return true;
        }
        return false;
    }
}
