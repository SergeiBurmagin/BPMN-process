package ru.starfish24.mascotte.bpm.exportreservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class LoyaltyCardResponseDto implements Serializable {
    @JsonProperty(value = "number")
    String number;
    @JsonProperty(value = "amount")
    String amount;
    @JsonProperty(value = "turnover")
    String turnover;

    public LoyaltyCardResponseDto() {
    }
}
