package ru.starfish24.mascotte.service.reservation;

import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.ItemDto;

import java.util.List;
import java.util.Map;

/**
 * Сервис используется  в ChangeReservationActivity для выполнения функциональных задач
 */
public interface ChangeReservationActivityService {


    /**
     * Получить список резерво по заказу с иденфтикатором @orderId
     *
     * @param orderId
     * @return Список всех резервов по заказу
     */
    List<InternalReservationDto> getInternalReservations(Long orderId);

    /**
     * Найти существующие резервы по заказу.
     * Существующими резервами считаем те, которые являются последними в цепочке. И находятся в статсе - выполнить резерв, зарезервировано, резерв провален.
     * Резерв статусе провален и зареезервировать -будут выполенны или отменены, но на момент определения их надо считать выполненными.
     *
     * @param reservations Список всех резервов по заказу
     * @return Список сущетвующих записей по резерву.
     */
    Map<Long, List<InternalReservationDto>> findExistReservations(List<InternalReservationDto> reservations);


    /**
     * Ищет разницу между заврезервирвонными позциями заказа, и теми что есть
     *
     * @param reserved Уже зарезервированные позиции заказа
     * @param items    список позиций заказа подлежащих резерву
     * @return ключ -  иденфтикатор товара, значение - разница между резервом и новым заказом.\
     */
    Map<Long, Double> findDiff(Map<Long, List<InternalReservationDto>> reserved, List<ItemDto> items);

}
