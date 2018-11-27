package ru.starfish24.mascotte.repository.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.attributes.warehouse.WarehouseAttribute;

@Repository
public interface WarehouseAttributeRepository extends JpaRepository<WarehouseAttribute, Long> {
    WarehouseAttribute findByCode(@Param("code") String code, @Param("accountId") Long accountId);
}
