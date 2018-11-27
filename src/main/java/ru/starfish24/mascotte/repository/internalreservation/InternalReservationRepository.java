package ru.starfish24.mascotte.repository.internalreservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.InternalReservation;

import java.util.List;

@Repository
public interface InternalReservationRepository extends JpaRepository<InternalReservation, Long> {

    @Query("SELECT r FROM InternalReservation r WHERE r.orderItem.id = :orderItemId")
    List<InternalReservation> findByOrderItemId(@Param("orderItemId") Long orderItemId);
}
