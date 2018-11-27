package ru.starfish24.mascotte.service.email;

public interface AmazonSEService {

    void sendEmail(String to, String subject, String body);
}
