package ru.starfish24.mascotte.config;

import io.swagger.client.ApiClient;
import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.api.OrderControllerApi;
import io.swagger.client.api.WarehouseControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EntityScan(value = "ru.starfish24.starfish24model")
public class AppConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "camundaBpmDataSource")
    @ConfigurationProperties(prefix = "camunda.datasource")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public InternalReservationControllerApi getInternalReservationControllerApi(ApiClient apiClient) {
        return new InternalReservationControllerApi(apiClient);
    }

    @Bean
    public OrderControllerApi getOrderControllerApi(ApiClient apiClient) {
        return new OrderControllerApi(apiClient);
    }

    @Bean
    public ApiClient getApiClient(RestTemplate restTemplate, @Value("${starfish.service.basepath}") String basepath, @Value("${starfish24.access.token}") String apiAccessToken) {
        ApiClient client = new ApiClient(restTemplate).setBasePath(basepath);
        client.addDefaultHeader("access_token", apiAccessToken);
        return client;
    }

    @Bean
    public WarehouseControllerApi warehouseControllerApi(ApiClient apiClient) {
        return new WarehouseControllerApi(apiClient);
    }

}
