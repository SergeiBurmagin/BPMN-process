package ru.starfish24.mascotte.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.starfish24model.IntegrationMicroservice;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    @Value("${starfish24.access.token}")
    private String accessToken;

    @Autowired
    IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @PostConstruct
    public void init() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder().addInterceptor(logging).build();
    }

    public String getRequest(String url, Map<String, String> queryParamters) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("access_token", accessToken);
        if (queryParamters != null) {
            queryParamters.entrySet().stream().forEach(entry -> httpBuilder.addQueryParameter(entry.getKey(), entry.getValue()));
        }
        Request requestHttp = new Request.Builder().url(httpBuilder.build()).get().build();

        return executeRequest(requestHttp);
    }

    public String getRequest(String url) {
        return getRequest(url, null);
    }

    public String postRequest(String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("access_token", accessToken);
        Request requestHttp = new Request.Builder().url(httpBuilder.build())
                .post(RequestBody.create(JSON, "")).build();

        return executeRequest(requestHttp);
    }

    public String postRequest(String url, Object request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(request));
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("access_token", accessToken);
        Request requestHttp = new Request.Builder().url(httpBuilder.build()).post(body).build();

        return executeRequest(requestHttp);
    }

    private String executeRequest(Request requestHttp) {
        try (Response response = client.newCall(requestHttp).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }
            throw new BpmnError(String.format("Unexpected code: %s", response));
        } catch (Exception e) {
            throw new BpmnError(String.format("Connection failed: %s", e));
        }
    }

    public String getRequest(Long accountId, String type, Set<String> pathVariables) {
        String uri = getUrl(accountId, type);
        if (pathVariables != null) {
            uri = String.format(uri, pathVariables.stream().toArray());
        }
        return getRequest(uri);
    }

    private String getUrl(Long accountId, String type) {
        IntegrationMicroservice ms = integrationMicroserviceRepository.findByAccountIdAndType(accountId, type);
        if (ms == null) {
            throw new IllegalArgumentException(String.format("Unable to find IntegrationMicroservice for [type= %s] and [accountId = %s]", type, accountId));
        }
        return ms.getUri();
    }

    public String getUrl(String type) {
        IntegrationMicroservice ms = integrationMicroserviceRepository.findByTypeEquals(type);
        if (ms == null) {
            throw new IllegalArgumentException(String.format("Unable to find IntegrationMicroservice for [type= %s]", type));
        }
        return ms.getUri();
    }

    public String postRequest(Long accountId, String type, Object request) throws IOException {
        String url = getUrl(accountId, type);
        return postRequest(url, request);
    }
}
