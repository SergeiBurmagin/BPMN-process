package ru.starfish24.mascotte.dto.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest implements Serializable {

    @JsonProperty("id")
    private String externalId;
    ;

    private Integer quantity;
}
