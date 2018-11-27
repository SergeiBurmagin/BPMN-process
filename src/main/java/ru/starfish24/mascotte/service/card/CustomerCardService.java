package ru.starfish24.mascotte.service.card;

import ru.starfish24.starfish24model.CustomerCard;

public interface CustomerCardService {
    /**
     * Метод вернет информацию по карте лояльности
     *
     * @param orderId - идентификатор заказа
     * @return Сущность клиентской карточки
     */
    CustomerCard getInfoByOrderId(Long orderId);
}
