package ie.universityofgalway.groupnine.service.email.port;

/**
 * Low-level email sending port (e.g., Mailjet implementation). Implementations are responsible for
 * interacting with an external provider and handling transient errors as appropriate.
 */
public interface SendEmailPort {
    /**
     * Sends an email message.
     *
     * @param fromEmail sender email address
     * @param fromName  sender display name
     * @param toEmail   recipient email address
     * @param subject   subject line (required)
     * @param htmlBody  HTML body (may be null if only text is provided)
     * @param textBody  plain-text alternative (optional)
     */
    void send(String fromEmail, String fromName, String toEmail, String subject, String htmlBody, String textBody);
}
