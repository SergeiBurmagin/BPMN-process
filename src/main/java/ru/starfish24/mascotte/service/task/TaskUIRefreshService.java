package ru.starfish24.mascotte.service.task;

public interface TaskUIRefreshService {
    /**
     * Отправляет http запрос на обновление всех задачь Rest = {/refresh/task/all}
     */
    void refreshSignal();
}
