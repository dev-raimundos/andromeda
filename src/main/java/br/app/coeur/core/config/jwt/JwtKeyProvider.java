package br.app.coeur.core.config.jwt;

import br.app.coeur.modules.authentication.model.JwtSigningKey;
import br.app.coeur.modules.authentication.service.JwtKeyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class JwtKeyProvider {
    private static final Duration CACHE_TTL = Duration.ofSeconds(5);
    private final JwtKeyService service;
    private final Clock clock;
    private final JwtKeyMaterialFactory materialFactory = new JwtKeyMaterialFactory();
    private final AtomicReference<CachedKey> cached = new AtomicReference<>();

    @Autowired
    public JwtKeyProvider(JwtKeyService service) {
        this(service, Clock.systemUTC());
    }

    JwtKeyProvider(JwtKeyService service, Clock clock) {
        this.service = service;
        this.clock = clock;
        this.cached.set(load(service.initialize(), clock.instant().plus(CACHE_TTL)));
    }

    public JwtEncoder jwtEncoder() {
        return parameters -> {
            CachedKey key = current();
            JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.id().toString()).build();
            return key.encoder().encode(JwtEncoderParameters.from(header, parameters.getClaims()));
        };
    }

    public JwtDecoder jwtDecoder() {
        return token -> current().decoder().decode(token);
    }

    @TransactionalEventListener
    public synchronized void onKeyRotated(JwtKeyService.KeyRotated event) {
        // Executed after commit: never cache key material from a rolled-back rotation.
        cached.set(null);
    }

    private CachedKey current() {
        CachedKey key = cached.get();
        if (key != null && clock.instant().isBefore(key.expiresAt())) return key;
        return refresh();
    }

    private synchronized CachedKey refresh() {
        Instant now = clock.instant();
        CachedKey key = cached.get();
        if (key != null && now.isBefore(key.expiresAt())) return key;
        JwtSigningKey stored = service.getActiveKey();
        CachedKey refreshed = key != null && key.id().equals(stored.getKeyId())
                ? new CachedKey(key.id(), key.encoder(), key.decoder(), now.plus(CACHE_TTL))
                : load(stored, now.plus(CACHE_TTL));
        cached.set(refreshed);
        return refreshed;
    }

    private CachedKey load(JwtSigningKey stored, Instant expiresAt) {
        JwtKeyMaterial material = materialFactory.create(stored);
        return new CachedKey(material.id(), material.encoder(), material.decoder(), expiresAt);
    }

    private record CachedKey(UUID id, JwtEncoder encoder, JwtDecoder decoder, Instant expiresAt) {
    }
}
