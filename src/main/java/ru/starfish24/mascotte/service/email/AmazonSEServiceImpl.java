package ru.starfish24.mascotte.service.email;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Service
@Slf4j
public class AmazonSEServiceImpl implements AmazonSEService {

    @Value("${starfish24.external.service.amazon.ses.secretKey}")
    private String secretKey;
    @Value("${starfish24.external.service.amazon.ses.accessKey}")
    private String accessKey;
    @Value("${starfish24.external.service.amazon.ses.region}")
    private String region;
    @Value("${starfish24.external.service.amazon.ses.emailFrom}")
    private String shopEmail;
    @Value("${starfish24.external.service.amazon.ses.nameFrom}")
    private String shopName;

    private AmazonSimpleEmailService client;

    public AmazonSEServiceImpl() {

    }

    @PostConstruct
    private void afterCreate() {
        Objects.requireNonNull(secretKey, "Cant read secret Key properties");
        Objects.requireNonNull(accessKey, "Cant read access Key properties");

        this.client = AmazonSimpleEmailServiceAsyncClient.asyncBuilder()
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)
                        )
                )
                .withRegion(Regions.fromName(region)).build();
    }

    @Override
    public void sendEmail(String to, String subject, String body) {

        try {
            //AmazonSESC
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(to))
                    .withMessage(
                            new Message()
                                    .withBody(new Body()
                                            .withText(new Content()
                                                    .withCharset("UTF-8").withData(body)))
                                    .withSubject(new Content()
                                            .withCharset("UTF-8").withData(subject))
                    ).withSource(getSender());
            client.sendEmail(request);
            log.info("Email to {} with text {} send!", to, body);
        } catch (Exception e) {
            log.info("Amazon SES error {}", e);
        }

    }

    private String getSender() {
        return String.format("%s <%s>", shopName, shopEmail);
    }
}
