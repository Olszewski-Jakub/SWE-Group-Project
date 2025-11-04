package ie.universityofgalway.groupnine.domain.email.jobs;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class EmailJobsTest {
    @Test
    void accountVerification_requiresLink_andBuildsModel() {
        AccountVerificationEmailJob job = AccountVerificationEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .verificationLink(URI.create("https://example/verify"))
                .userName("Jane")
                .locale(Locale.GERMAN)
                .priority(Priority.HIGH)
                .id(EmailJobId.newId())
                .createdAt(Instant.now())
                .build();
        assertEquals(EmailType.ACCOUNT_VERIFICATION, job.type());
        assertEquals("user@example.com", job.to().value());
        assertEquals(URI.create("https://example/verify"), job.templateModel().get("verificationLink"));
        assertEquals("Jane", job.templateModel().get("userName"));
    }

    @Test
    void welcome_allowsOptionalName() {
        WelcomeEmailJob job = WelcomeEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .userName("John")
                .build();
        assertEquals(EmailType.WELCOME, job.type());
        assertEquals("John", job.templateModel().get("userName"));
    }

    @Test
    void orderPaid_containsTotals() {
        OrderPaidEmailJob job = OrderPaidEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("abc-123")
                .amountMinor(1299L)
                .currency("EUR")
                .build();
        assertEquals(EmailType.ORDER_PAID, job.type());
        assertEquals("abc-123", job.templateModel().get("orderId"));
        assertEquals(1299L, job.templateModel().get("amountMinor"));
        assertEquals("EUR", job.templateModel().get("currency"));
    }

    @Test
    void paymentFailed_containsReason() {
        PaymentFailedEmailJob job = PaymentFailedEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("abc")
                .reason("DECLINED")
                .build();
        assertEquals(EmailType.PAYMENT_FAILED, job.type());
        assertEquals("DECLINED", job.templateModel().get("reason"));
    }

    @Test
    void orderRefunded_containsTotals() {
        OrderRefundedEmailJob job = OrderRefundedEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("abc-123")
                .amountMinor(599L)
                .currency("EUR")
                .build();
        assertEquals(EmailType.ORDER_REFUNDED, job.type());
        assertEquals(599L, job.templateModel().get("amountMinor"));
        assertEquals("EUR", job.templateModel().get("currency"));
    }

    @Test
    void passwordReset_requiresEmail_andBuilds() {
        PasswordResetEmailJob job = PasswordResetEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .resetLink(URI.create("https://example/reset"))
                .userName("User")
                .build();
        assertEquals(EmailType.PASSWORD_RESET, job.type());
        assertEquals(URI.create("https://example/reset"), job.templateModel().get("resetLink"));
    }
}

