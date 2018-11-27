package ru.starfish24.mascotte;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableProcessApplication
@EnableScheduling
public class MascotteProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MascotteProcessApplication.class, args);
    }

}
