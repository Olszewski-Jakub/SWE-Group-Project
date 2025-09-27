package ie.universityofgalway.groupnine.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HS256 JWT creation and validation service.
 * <p>
 * - Uses HMAC secret from {@link AppSecurityProps.Jwt}
 * - Supports Base64 or raw secrets (>= 32 bytes)
 * - Embeds/validates optional issuer
 * - Roles claim mapped by configured name
 */
@Service
public class JwtService {

    private static final AppLogger LOG = AppLogger.get(JwtService.class);
    private final AppSecurityProps props;
    private final Clock clock;

    public JwtService(AppSecurityProps props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    private static byte[] resolveSecretKeyBytes(String secret) {
        // Try Base64 decode, fall back to raw bytes if it fails
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String createAccessToken(String subjectUserId, List<String> roles, Map<String, Object> extraClaims) {
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        List<String> safeRoles = roles == null ? List.of() : roles;
        Map<String, Object> safeExtra = extraClaims == null ? Map.of() : extraClaims;

        Instant now = clock.instant();
        Instant exp = now.plus(props.getJwt().getAccessTokenTtl());

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(subjectUserId)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp));

        if (props.getJwt().getIssuer() != null && !props.getJwt().getIssuer().isBlank()) {
            builder.issuer(props.getJwt().getIssuer());
        }

        // add roles claim
        builder.claim(props.getJwt().getAuthoritiesClaim(), safeRoles);
        // add extra claims
        safeExtra.forEach(builder::claim);

        JWTClaimsSet claims = builder.build();

        byte[] key = resolveSecretKeyBytes(props.getJwt().getHmacSecret());
        ensureMinKeyLength(key);
        SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            signed.sign(new MACSigner(key));
        } catch (JOSEException e) {
            LOG.error("jwt_sign_failed", "error", e.getMessage());
            throw new JwtException("Failed to sign JWT", e);
        }
        String token = signed.serialize();
        LOG.info(
                "jwt_created",
                "sub", subjectUserId,
                "roles_count", safeRoles.size(),
                "iss", props.getJwt().getIssuer() == null ? "" : props.getJwt().getIssuer(),
                "exp", DateTimeFormatter.ISO_INSTANT.format(exp)
        );
        return token;
    }

    public JWTClaimsSet validate(String token) {
        try {
            SignedJWT signed = SignedJWT.parse(token);
            byte[] key = resolveSecretKeyBytes(props.getJwt().getHmacSecret());
            ensureMinKeyLength(key);
            boolean ok = signed.verify(new MACVerifier(key));
            if (!ok) {
                LOG.warn("jwt_invalid", "reason", "bad_signature");
                throw new JwtException("Invalid token signature");
            }

            JWTClaimsSet claims = getJwtClaimsSet(signed);
            LOG.debug(
                    "jwt_valid",
                    "sub", claims.getSubject() == null ? "" : claims.getSubject(),
                    "iss", claims.getIssuer() == null ? "" : claims.getIssuer(),
                    "exp", claims.getExpirationTime() == null ? "" : claims.getExpirationTime().toInstant().toString()
            );
            return claims;
        } catch (ParseException e) {
            LOG.warn("jwt_invalid", "reason", "parse_error", "error", e.getMessage());
            throw new JwtException("Invalid token format", e);
        } catch (JOSEException e) {
            LOG.warn("jwt_invalid", "reason", "verification_error", "error", e.getMessage());
            throw new JwtException("JWT verification error", e);
        }
    }

    private JWTClaimsSet getJwtClaimsSet(SignedJWT signed) throws ParseException {
        JWTClaimsSet claims = signed.getJWTClaimsSet();

        // exp
        Date exp = claims.getExpirationTime();
        Date now = Date.from(clock.instant());
        if (exp == null || !exp.after(now)) {
            LOG.warn("jwt_invalid", "reason", "expired");
            throw new JwtException("Token expired");
        }
        // iss (optional)
        String expectedIssuer = props.getJwt().getIssuer();
        if (expectedIssuer != null && !expectedIssuer.isBlank()) {
            if (!expectedIssuer.equals(claims.getIssuer())) {
                LOG.warn("jwt_invalid", "reason", "bad_issuer", "expected", expectedIssuer, "got", claims.getIssuer() == null ? "" : claims.getIssuer());
                throw new JwtException("Invalid issuer");
            }
        }
        return claims;
    }

    private void ensureMinKeyLength(byte[] key) {
        // HS256 requires >= 256-bit (32 bytes) key
        if (key == null || key.length < 32) {
            LOG.error(
                    "weak_hmac_secret",
                    "provided_bytes", key == null ? 0 : key.length,
                    "required_min_bytes", 32
            );
            throw new JwtException("HMAC secret too short (need at least 32 bytes). Set JWT_SECRET to a strong value, e.g., `openssl rand -base64 32`.");
        }
    }
}
