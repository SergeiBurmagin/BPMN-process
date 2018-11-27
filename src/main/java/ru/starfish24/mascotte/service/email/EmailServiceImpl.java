package ru.starfish24.mascotte.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${starfish24.mail.smtp.host}")
    private String host;

    @Override
    public void sendMessage(String addresses, String subject, String text) {
        log.info("Sending mail subject:'{}' >>> %s", subject, addresses);

        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 25);
        props.put("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(props);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("oms@zenden.ru"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addresses));
            msg.setSubject(subject);
            msg.setHeader("Content-Type", "text/html; charset=UTF-8");
            msg.setContent(text, "text/html; charset=UTF-8");
            Transport.send(msg);
        } catch (MessagingException e) {
            log.error("Can't send mail to: {}", addresses, e);
        }
    }

    @Override
    public void sendMessage(String addresses, String subject, String text, String attachment) {

    }
}
