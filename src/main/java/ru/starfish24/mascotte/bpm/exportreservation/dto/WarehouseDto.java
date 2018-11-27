package ru.starfish24.mascotte.bpm.exportreservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Example:
 * "name": "брак Сити Молл",
 * "isDefault": null,
 * "district": null,
 * "brand": null,
 * "c": null,
 * "city": null,
 * "address": null,
 * "comments": null,
 * "worksFrom": null,
 * "worksTill": null,
 * "externalId": "ef2f214a-cb68-11e6-80d0-002590341539",
 * "priority": null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WarehouseDto implements Serializable {

    private String name;

    @JsonProperty("isDefault")
    private Boolean usedByDefault;

    private Boolean pointOfSale;

    private String externalId;

    private String code;

}
