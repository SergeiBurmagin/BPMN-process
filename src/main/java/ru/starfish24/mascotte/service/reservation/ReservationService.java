package ru.starfish24.mascotte.service.reservation;

import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationRequest;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.TransitionDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.dto.reservation.Product;

import java.io.IOException;
import java.util.List;

public interface ReservationService {

    /**
     * Посылает в шину запрос о резервирование товаров в заказе
     *
     * @param orderId                    Идентификатор заказа
     * @param products                   Инфорация по товарам котоыре должны быть зарезервированы
     * @param warehouseExternalId        Идентификатор склада на котором происходить резерв
     * @param transitWarehouseExternalId Иденфтикатор транзитного склада
     * @param targetWarehouseExternalId  Идентификатор целевого склада
     * @return Ответ от шины данных, по резервированию позиций заказа.
     */
    IntegrationServiceResponse createReservation(Long orderId, List<Product> products, String warehouseExternalId, String transitWarehouseExternalId, String targetWarehouseExternalId);

    /**
     * Формирует список оперций по резервированию с информацией по необходимости перемещения товара.
     *
     * @param reservationInfoes Информация по доставке товара из Order-Broker
     * @return Список описаний действий по резервированию корые нужно выполнить.
     */
    List<InternalReservationDto> createInternalReservations(List<ShipToCustomerResultDto> reservationInfoes);

    /**
     * Создать перемещение группы товаров.
     * * Посылает запрос в интеграционную шину о том что товары должны быть перемещены.
     *
     * @param orderId        Иденфикатор заказа
     * @param products       Список доступных товаров для перемещения
     * @param externalFromId Иденфтикатор склада с которого осуществляется пермещение
     * @param externalToId   Идентификтор склада на который осуществляется пермещение.	 *
     * @return Список описаний действий по резервированию корые нужно выполнить.
     */
    IntegrationServiceResponse createTransfer(@NonNull Long orderId, List<Product> products, String externalToId, String externalFromId);

    @Transactional
    InternalReservationDto confirmInternalTransfer(InternalReservationDto internalReservationDto);

    @Transactional
    InternalReservationDto cancelInternalTransfer(InternalReservationDto internalReservationDto);

    @Transactional
    InternalReservationDto confirmInternalReservation(InternalReservationDto reservationDto, String confirmID);

    @Transactional
    InternalReservationDto internalReservationFailed(InternalReservationDto internalReservationDto);

    /**
     * Обновить статус резервирония на "Резервирование отменено"
     *
     * @param internalReservationDto информация по резервированию
     * @return Обновленные данные по резервирование позиции заказа
     */
    InternalReservationDto internalReservationCanceled(InternalReservationDto internalReservationDto);


    /**
     * Создать в запись с информацией о резерве
     *
     * @param status      Статус резерва
     * @param quantity    Колличество резервируемого товара
     * @param target      Целевой склад
     * @param reserve     Склад резерва
     * @param transit     Транзитный склад
     * @param orderItemId идентфикатор позиции заказа
     * @param transition  Информация по перемещению
     * @return Информация о резервирование товара после сохранения
     */
    InternalReservationDto createInternalReservationDto(InternalReservationRequest.StatusEnum status, double quantity, WarehouseDto target, WarehouseDto reserve, WarehouseDto transit, Long orderItemId, TransitionDto transition);

    /**
     * @param status             Статус резерва
     * @param quantity           Колличество резервируемого товара
     * @param orderItemId        Идентификатор позиции заказа
     * @param targetWarehouseId  идендитифкатор целевого склада
     * @param reserveWarehouseId идентифиатор резервного склада
     * @param transitWarehouseId идентификатор транзитного склада
     * @param fromWarehouseId    идентфикатор склада откуда нужно совержить пермещение
     * @param toWarehouseId      иденфтикатор склада куда нужно совершить перемещение.
     * @return Информация о резервирование товара после сохранения
     */
    InternalReservationDto createInternalReservationDto(InternalReservationRequest.StatusEnum status, double quantity, Long orderItemId, Long targetWarehouseId, Long reserveWarehouseId, Long transitWarehouseId, Long fromWarehouseId, Long toWarehouseId);

    /**
     * Выполнить запрос на отмену перемещения товоара
     *
     * @param internalReservationDto данные по резервированию и перемещению товара
     * @return Результат выполнения операции
     */
    IntegrationServiceResponse cancelTransfer(InternalReservationDto internalReservationDto) throws IOException;

    /**
     * Выполнить запрос на отмену резерва товара
     *
     * @param internalReservationDto данные по резервированию и перемещению товара.
     * @return Результат выполнения операции
     */
    IntegrationServiceResponse cancelReserve(InternalReservationDto internalReservationDto) throws IOException;

    void doReservationByInternalReservationDtoList(Long orderId, List<InternalReservationDto> internalReservationDtos)
            throws IOException;

    void doTransferByInternalReservationDtoList(Long orderId, List<InternalReservationDto> internalReservationDtos)
            throws IOException;

    List<Product> getProducts(List<InternalReservationDto> toGroup);

    Long getWarehouseId(WarehouseDto dto);
}
