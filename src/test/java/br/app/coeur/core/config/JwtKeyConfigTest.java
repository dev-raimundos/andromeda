package br.app.coeur.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class JwtKeyConfigTest {
    @TempDir Path directory;

    @Test
    void developmentPersistsKeysAndProductionReusesThem() {
        var pub = new FileSystemResource(directory.resolve("public.pem"));
        var priv = new FileSystemResource(directory.resolve("private.pem"));
        var dev = new MockEnvironment();
        dev.setActiveProfiles("dev");
        var first = new JwtKeyConfig(pub, priv, dev);
        var prod = new MockEnvironment();
        prod.setActiveProfiles("prod");
        var restarted = new JwtKeyConfig(pub, priv, prod);
        assertThat(restarted.publicKey().getEncoded()).isEqualTo(first.publicKey().getEncoded());
        assertThat(restarted.privateKey().getEncoded()).isEqualTo(first.privateKey().getEncoded());
    }

    @Test
    void productionRequiresAnExistingKeyPair() {
        var prod = new MockEnvironment();
        prod.setActiveProfiles("prod");
        assertThatThrownBy(() -> new JwtKeyConfig(new FileSystemResource(directory.resolve("pub.pem")),
                new FileSystemResource(directory.resolve("priv.pem")), prod)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsMismatchedKeys() {
        var dev = new MockEnvironment();
        dev.setActiveProfiles("dev");
        var pub = new FileSystemResource(directory.resolve("pub.pem"));
        var priv = new FileSystemResource(directory.resolve("priv.pem"));
        var otherPub = new FileSystemResource(directory.resolve("other-pub.pem"));
        var otherPriv = new FileSystemResource(directory.resolve("other-priv.pem"));
        new JwtKeyConfig(pub, priv, dev);
        new JwtKeyConfig(otherPub, otherPriv, dev);
        assertThatThrownBy(() -> new JwtKeyConfig(pub, otherPriv, dev)).hasRootCauseMessage("JWT public and private keys do not match.");
    }
}
