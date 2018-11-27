package ru.starfish24.mascotte.repository.integrationmicroservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.IntegrationMicroservice;

@Repository
public interface IntegrationMicroserviceRepository extends JpaRepository<IntegrationMicroservice, Long> {

    @Query("SELECT m FROM IntegrationMicroservice m WHERE m.account.id = :accountId AND m.type = :type")
    IntegrationMicroservice findByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") String type);

    IntegrationMicroservice findByTypeEquals(String type);

}
