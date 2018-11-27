package ru.starfish24.mascotte.repository.status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.Status;

import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    List<Status> findByAccount(@Param("accountId") Long accountId);
}
