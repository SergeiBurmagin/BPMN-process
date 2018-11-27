package ru.starfish24.mascotte.bpm.zenden;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ExportOrderToSiteActivity implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // todo сюда занести реализацию, "Отправить заказ на сайт"
    }
}
