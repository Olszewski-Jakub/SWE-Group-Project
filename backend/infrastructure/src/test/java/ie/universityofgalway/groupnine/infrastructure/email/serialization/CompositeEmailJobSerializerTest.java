package ie.universityofgalway.groupnine.infrastructure.email.serialization;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.AccountVerificationEmailJob;
import ie.universityofgalway.groupnine.domain.email.jobs.PasswordResetEmailJob;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import ie.universityofgalway.groupnine.infrastructure.email.serialization.codec.AccountVerificationEmailJobCodec;
import ie.universityofgalway.groupnine.infrastructure.email.serialization.codec.PasswordResetEmailJobCodec;
import ie.universityofgalway.groupnine.infrastructure.email.serialization.codec.WelcomeEmailJobCodec;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CompositeEmailJobSerializerTest {

    @Test
    void roundTrip_accountVerification() {
        CompositeEmailJobSerializer s = new CompositeEmailJobSerializer(List.of(
                new AccountVerificationEmailJobCodec(), new PasswordResetEmailJobCodec(), new WelcomeEmailJobCodec()
        ));
        var job = AccountVerificationEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .verificationLink(URI.create("https://verify"))
                .id(EmailJobId.newId())
                .locale(Locale.ENGLISH)
                .priority(Priority.HIGH)
                .userName("Alice")
                .build();
        byte[] bytes = s.toBytes(job);
        var parsed = s.fromBytes(bytes);
        assertEquals(job.type(), parsed.type());
        assertEquals(job.to().value(), parsed.to().value());
    }

    @Test
    void roundTrip_passwordReset() {
        CompositeEmailJobSerializer s = new CompositeEmailJobSerializer(List.of(
                new AccountVerificationEmailJobCodec(), new PasswordResetEmailJobCodec(), new WelcomeEmailJobCodec()
        ));
        var job = PasswordResetEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .resetLink(URI.create("https://reset"))
                .id(EmailJobId.newId())
                .locale(Locale.ENGLISH)
                .priority(Priority.NORMAL)
                .build();
        byte[] bytes = s.toBytes(job);
        var parsed = s.fromBytes(bytes);
        assertEquals(job.type(), parsed.type());
        assertEquals(job.to().value(), parsed.to().value());
    }

    @Test
    void roundTrip_welcome() {
        CompositeEmailJobSerializer s = new CompositeEmailJobSerializer(List.of(
                new AccountVerificationEmailJobCodec(), new PasswordResetEmailJobCodec(), new WelcomeEmailJobCodec()
        ));
        var job = WelcomeEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .id(EmailJobId.newId())
                .locale(Locale.ENGLISH)
                .priority(Priority.NORMAL)
                .userName("Bob")
                .build();
        byte[] bytes = s.toBytes(job);
        var parsed = s.fromBytes(bytes);
        assertEquals(job.type(), parsed.type());
        assertEquals(job.to().value(), parsed.to().value());
    }
}

