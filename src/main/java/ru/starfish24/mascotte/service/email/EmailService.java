package ru.starfish24.mascotte.service.email;

public interface EmailService {

    void sendMessage(String addresses, String subject, String text);

    void sendMessage(String addresses, String subject, String text, String attachment);
}
