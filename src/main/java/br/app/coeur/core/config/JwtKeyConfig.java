package br.app.coeur.core.config;

import br.app.coeur.modules.authentication.model.JwtSigningKey;
import br.app.coeur.modules.authentication.service.JwtKeyService;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtKeyConfig {
    private static final Duration CACHE_TTL = Duration.ofSeconds(5);
    private final JwtKeyService service;
    private final Clock clock;
    private volatile CachedKey cached;

    @Autowired
    public JwtKeyConfig(JwtKeyService service) {
        this(service, Clock.systemUTC());
    }

    JwtKeyConfig(JwtKeyService service, Clock clock) {
        this.service = service;
        this.clock = clock;
        this.cached = load(service.initialize(), clock.instant().plus(CACHE_TTL));
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return parameters -> {
            CachedKey key = current();
            JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.id().toString()).build();
            return key.encoder().encode(JwtEncoderParameters.from(header, parameters.getClaims()));
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> current().decoder().decode(token);
    }

    @TransactionalEventListener
    public synchronized void onKeyRotated(JwtKeyService.KeyRotated event) {
        // Executed after commit: never cache key material from a rolled-back rotation.
        cached = null;
    }

    private CachedKey current() {
        CachedKey key = cached;
        if (key != null && clock.instant().isBefore(key.expiresAt())) return key;
        return refresh();
    }

    private synchronized CachedKey refresh() {
        Instant now = clock.instant();
        CachedKey key = cached;
        if (key != null && now.isBefore(key.expiresAt())) return key;
        JwtSigningKey stored = service.getActiveKey();
        cached = key != null && key.id().equals(stored.getKeyId())
                ? new CachedKey(key.id(), key.encoder(), key.decoder(), now.plus(CACHE_TTL))
                : load(stored, now.plus(CACHE_TTL));
        return cached;
    }

    private CachedKey load(JwtSigningKey stored, Instant expiresAt) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(stored.getPublicKey())));
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(stored.getPrivateKey())));
            Signature signature = Signature.getInstance("SHA256withRSA");
            byte[] challenge = new byte[]{1, 2, 3};
            signature.initSign(privateKey);
            signature.update(challenge);
            byte[] signed = signature.sign();
            signature.initVerify(publicKey);
            signature.update(challenge);
            if (!signature.verify(signed)) throw new IllegalStateException("JWT keys in the database do not match.");
            RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(stored.getKeyId().toString()).build();
            JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("coeur-api"));
            return new CachedKey(stored.getKeyId(), encoder, decoder, expiresAt);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load JWT signing key from the database", e);
        }
    }

    private record CachedKey(UUID id, JwtEncoder encoder, JwtDecoder decoder, Instant expiresAt) {
    }
}
