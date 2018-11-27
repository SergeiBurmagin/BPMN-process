package ru.starfish24.mascotte.repository.internalreservationstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.InternalReservationStatus;

@Repository
public interface InternalReservationStatusRepository extends JpaRepository<InternalReservationStatus, Long> {

    @Query("SELECT s FROM InternalReservationStatus s WHERE s.code = :code and s.account.id = :accountId")
    InternalReservationStatus findByCodeAndAccountId(@Param("code") String code, @Param("accountId") Long accountId);
}
