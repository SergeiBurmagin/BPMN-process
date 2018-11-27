package ru.starfish24.mascotte.repository.productorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.ProductOrder;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    ProductOrder getOneByExternalId(String externalOrderId);

    @Query("SELECT o FROM ProductOrder o WHERE o.currentStatus.code IN(:statuses) AND o.timeCreated >= :daysOld AND o.shop.account.activitiKey = :accountType")
    List<ProductOrder> findAllByStatusAndDaysOld(@Param("accountType") String accountType, @Param("statuses") Set<String> statuses, @Param("daysOld") Date daysOld);
}
