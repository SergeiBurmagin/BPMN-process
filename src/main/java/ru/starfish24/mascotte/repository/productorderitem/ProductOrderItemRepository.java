package ru.starfish24.mascotte.repository.productorderitem;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.starfish24.starfish24model.ProductOrderItem;

public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItem, Long> {
}
