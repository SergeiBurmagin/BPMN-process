package ru.starfish24.mascotte.repository.shipping;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.starfish24.starfish24model.Shipping;

public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    Shipping getByExternalId(String externalId);
}
