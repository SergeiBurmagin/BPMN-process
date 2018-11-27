package ru.starfish24.mascotte.service.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.model.InternalReservationDto;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExecuteReservationServiceImpl implements ExecuteReservationService {

    private final ObjectMapper objectMapper;

    public ExecuteReservationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toString(InternalReservationDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InternalReservationDto toDto(String dto) {
        try {
            return objectMapper.readValue(dto, InternalReservationDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
