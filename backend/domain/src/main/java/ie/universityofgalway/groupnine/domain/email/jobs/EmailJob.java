package ie.universityofgalway.groupnine.domain.email.jobs;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

/**
 * Marker interface for email jobs handled by the worker. Implementations provide the template
 * model and identify their {@link EmailType}.
 */
public sealed interface EmailJob permits AccountVerificationEmailJob, OrderPaidEmailJob, OrderRefundedEmailJob, PasswordResetEmailJob, PaymentFailedEmailJob, WelcomeEmailJob {
    EmailJobId id();

    EmailType type();

    EmailAddress to();

    Locale locale();

    Map<String, Object> templateModel();

    Instant createdAt();

    Priority priority();
}
