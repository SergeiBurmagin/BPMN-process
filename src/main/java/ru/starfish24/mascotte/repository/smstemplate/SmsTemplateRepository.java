package ru.starfish24.mascotte.repository.smstemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starfish24.starfish24model.tempates.SmsTemplate;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    SmsTemplate findByAccountIdAndCode(@Param("accountId") Long accountId, @Param("code") String code);
}
