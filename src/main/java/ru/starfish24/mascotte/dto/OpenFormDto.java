package ru.starfish24.mascotte.dto;

import lombok.Data;

@Data
public class OpenFormDto {

    private String taskId;
    private String assignee;
    private String formKey;
    private String processInstanceId;
}
