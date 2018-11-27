package ru.starfish24.mascotte.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;

@Component
@Slf4j
public class ProcessEventProducer {

    @Value("${kafka.topic.bpm.process.event}")
    private String processEventTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public ListenableFuture<SendResult<String, String>> sendMessage(Map<String, Object> variables) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(variables);
        ListenableFuture<SendResult<String, String>> futureResult = kafkaTemplate.send(processEventTopic, message);

        log.info("send message {} to {}", message, processEventTopic);
        return futureResult;
    }

}