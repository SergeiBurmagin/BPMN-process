package ru.starfish24.mascotte.dto.transition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.starfish24.mascotte.dto.reservation.Product;
import ru.starfish24.mascotte.dto.reservation.ProductRequest;

import java.util.List;

@Data
public class CreateTransitionDto {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("warehouseId")
    private String warehouseId;

    @JsonProperty("targetWarehouseId")
    private String targetWarehouseId;

    @JsonProperty("skuList")
    private List<ProductRequest> skuList;
}
