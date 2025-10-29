package ie.universityofgalway.groupnine.infrastructure.email.adapter;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link ie.universityofgalway.groupnine.service.email.port.SendEmailPort} implementation backed by
 * the Mailjet v3.1 API. Composes messages with subject, HTML and optional text body.
 */
@Component
public class MailjetEmailSenderAdapter implements SendEmailPort {
    private static final AppLogger log = AppLogger.get(MailjetEmailSenderAdapter.class);

    private final MailjetClient client;

    public MailjetEmailSenderAdapter(
            @Value("${mailjet.apiKey}") String apiKey,
            @Value("${mailjet.apiSecret}") String apiSecret
    ) {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("Mailjet API credentials are required (mailjet.apiKey, mailjet.apiSecret)");
        }
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecret)
                .build();
        this.client = new MailjetClient(options);
    }

    @Override
    public void send(String fromEmail, String fromName, String toEmail, String subject, String htmlBody, String textBody) {
        try {
            TransactionalEmail email = TransactionalEmail
                    .builder()
                    .from(new SendContact(fromEmail, fromName))
                    .to(new SendContact(toEmail))
                    .htmlPart(htmlBody)
                    .subject(subject)
                    .build();

            SendEmailsRequest request = SendEmailsRequest
                    .builder()
                    .message(email)
                    .build();
            try {
                SendEmailsResponse response = request.sendWith(client);
            } catch (MailjetException e) {
                log.error("Mailjet API credentials are invalid", e);
                throw e;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Mailjet send failed", e);
        }
    }
}
