package ru.starfish24.mascotte.dto;

import lombok.Data;

@Data
public class IntegrationServiceResponse {

    private String result;
    private String message;
    private Boolean success;
}
