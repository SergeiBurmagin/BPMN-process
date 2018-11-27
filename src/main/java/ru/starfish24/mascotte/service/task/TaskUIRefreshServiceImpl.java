package ru.starfish24.mascotte.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.starfish24.mascotte.http.HttpClient;

import java.io.IOException;
import java.net.SocketException;

@Service
@Slf4j
public class TaskUIRefreshServiceImpl implements TaskUIRefreshService {

    private final HttpClient httpClient;

    @Autowired
    public TaskUIRefreshServiceImpl(HttpClient httpClient) throws SocketException {
        this.httpClient = httpClient;
    }

    @Override
    public void refreshSignal() {
        try {
            httpClient.postRequest(httpClient.getUrl("starfish-ui-refresh"));
        } catch (IOException e) {
            log.error("Cant refresh task list {}", e);
        }
    }
}
