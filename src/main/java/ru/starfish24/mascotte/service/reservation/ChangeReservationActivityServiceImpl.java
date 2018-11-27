package ru.starfish24.mascotte.service.reservation;

import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationDto.StatusEnum;
import io.swagger.client.model.ItemDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ChangeReservationActivityServiceImpl implements ChangeReservationActivityService {

    private final InternalReservationControllerApi internalReservationControllerApi;

    public ChangeReservationActivityServiceImpl(InternalReservationControllerApi internalReservationControllerApi) {
        this.internalReservationControllerApi = internalReservationControllerApi;
    }

    public List<InternalReservationDto> getInternalReservations(Long orderId) {
        return internalReservationControllerApi.findByOrderIdUsingGET(orderId);
    }

    @Override
    public Map<Long, List<InternalReservationDto>> findExistReservations(List<InternalReservationDto> reservations) {
        Map<Long, List<InternalReservationDto>> result = new HashMap<>();

        for (InternalReservationDto reservationDto : reservations) {
            // просматриваем все резервы кроме MANUAL_RESERVE_WAREHOUSE
            if (!reservationDto.getStatus().equals(StatusEnum.MANUAL_RESERVE_WAREHOUSE)) {
                if (!result.containsKey(reservationDto.getProductId())) {
                    result.put(reservationDto.getProductId(), new ArrayList<>());
                }
                result.get(reservationDto.getProductId()).add(reservationDto);
            }
        }
        return result;
    }

    /**
     * Ищет разницу между заврезервирвонными позциями заказа, и теми что есть
     *
     * @param reserved Уже зарезервированные позиции заказа
     * @param items    список позиций заказа подлежащих резерву
     * @return ключ -  иденфтикатор товара, значение - разница между резервом и новым заказом.
     */
    @Override
    public Map<Long, Double> findDiff(Map<Long, List<InternalReservationDto>> reserved, List<ItemDto> items) {
        Map<Long, Double> result = new HashMap<>();
        Set<ItemDto> foundItems = items.stream()
                .filter(it -> reserved.containsKey(it.getId()))
                .collect(Collectors.toSet());
        for (ItemDto item : foundItems) {
            result.put(
                    item.getId(),
                    calculateDiff(item.getQuantity(), reserved.get(item.getId()))
            );
        }
        result = result.entrySet().stream().filter(pair -> pair.getValue() != 0).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        for (ItemDto item : items.stream().filter(i -> !reserved.containsKey(i.getId())).collect(Collectors.toList())) {
            result.put(item.getId(), new Double(item.getQuantity()));
        }
        return result;
    }

    /**
     * Метод высчитывает дельту резервирования
     *
     * @param source       текущте кол-во товара
     * @param reservations История резервации
     * @return Дельту, признак изминений в кол-ве товара сопостовимый с текущими резервами
     */
    private Double calculateDiff(int source, List<InternalReservationDto> reservations) {
        int currentReservations = calculateReservationSum(reservations);
        if (source == 0) {
            if (currentReservations > 0) {
                return (double) -currentReservations;
            }
            return 0d;
        }
        if (currentReservations > 0) return (double) (source - currentReservations);
        return (double) (source + currentReservations);
    }

    private int calculateReservationSum(List<InternalReservationDto> reservations) {
        int result = 0;
        for (InternalReservationDto dto : reservations) {
            // RESERVED || DO_RESERVE || RESERVE_FAILED || MANUAL_RESERVE_WAREHOUSE
            if (dto.getStatus().equals(StatusEnum.RESERVED) ||
                    dto.getStatus().equals(StatusEnum.DO_RESERVE) ||
                    dto.getStatus().equals(StatusEnum.RESERVE_FAILED) ||
                    dto.getStatus().equals(StatusEnum.MANUAL_RESERVE_WAREHOUSE)) {
                result = result + dto.getQuantity().intValue();
            } else {
                result = result - dto.getQuantity().intValue();
            }
        }
        return result;
    }
}
