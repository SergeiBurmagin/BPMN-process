package ru.starfish24.mascotte.repository.cancellationreason;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.starfish24.starfish24model.CancellationReason;

public interface CancellationReasonRepository extends JpaRepository<CancellationReason, Long> {

    CancellationReason findByCodeAndShopId(@Param("code") String code, @Param("shopId") Long shopId);
}
