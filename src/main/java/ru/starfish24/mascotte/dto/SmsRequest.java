package ru.starfish24.mascotte.dto;

import lombok.Data;

@Data
public class SmsRequest {

    private String message;

    private String recipient;

    private String sendAfter;

    private String sendBefore;

    private String clientId;

    private String secretKey;
}
