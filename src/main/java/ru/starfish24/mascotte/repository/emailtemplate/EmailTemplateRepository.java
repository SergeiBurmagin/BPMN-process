package ru.starfish24.mascotte.repository.emailtemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.starfish24.starfish24model.tempates.EmailTemplate;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    EmailTemplate findByCodeAndAccount(@Param("code") String code, @Param("accountId") Long accountId);
}
