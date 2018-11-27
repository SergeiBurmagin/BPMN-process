package ru.starfish24.mascotte.entity.listener;

import ru.starfish24.starfish24model.ProductOrder;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class HistoryListener {

    @PrePersist
    public void prePersist(ProductOrder productOrder) {
        productOrder.setCreateDate(new Date());
        productOrder.setModifyDate(productOrder.getCreateDate());
        productOrder.setCreator("mascotte-process");
        productOrder.setLastUpdator("mascotte-process");

    }

    @PreUpdate
    public void preUpdate(ProductOrder productOrder) {
        productOrder.setModifyDate(new Date());
        productOrder.setLastUpdator("mascotte-process");
    }
}
