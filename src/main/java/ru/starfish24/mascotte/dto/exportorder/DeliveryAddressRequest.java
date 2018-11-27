package ru.starfish24.mascotte.dto.exportorder;

import lombok.Data;

@Data
public class DeliveryAddressRequest {

    private String text;

    private String city;

    private String street;

    private String building;

    private String housing;

    private String flat;

    private String zip;

    private String metro;

    private String pickupShop;

    private String regionId;

    private String region;
}
