package ru.starfish24.mascotte.repository.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId and s.warehouse.externalId = :warehouseExternalId")
    Stock findByProductIdAndWarehouseExternalId(@Param("productId") Long productId, @Param("warehouseExternalId") String warehouseExternalId);
}
