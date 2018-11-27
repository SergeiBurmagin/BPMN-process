package ru.starfish24.mascotte.repository.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.attributes.warehouse.WarehouseAttributeValue;

import java.util.List;

@Repository
public interface WarehouseAttributeValueRepository extends JpaRepository<WarehouseAttributeValue, Long> {
    List<WarehouseAttributeValue> allObjectAttributeValues(@Param("ids") List<Long> ids);

    WarehouseAttributeValue findByCodeObject(@Param("code") String code, @Param("objectId") Long objectId);
}
