package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.user.Email;

/**
 * Port for sending auth-related emails.
 */
public interface EmailSenderPort {
    /**
     * Sends an email containing a verification link to the given address.
     *
     * @param to        destination email address
     * @param verifyUrl absolute URL containing the opaque token
     */
    void sendVerificationEmail(Email to, String verifyUrl);
}
