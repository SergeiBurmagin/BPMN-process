package ru.starfish24.mascotte.repository.customercards;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.starfish24.starfish24model.CustomerCard;

public interface CustomerCardsRepository extends JpaRepository<CustomerCard, Long> {
    CustomerCard getFirstByExternalIdEquals(String externalId);
}
