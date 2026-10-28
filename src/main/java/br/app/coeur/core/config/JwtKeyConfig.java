package br.app.coeur.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyConfig(
            @Value("${app.jwt.public-key:file:certs/jwt-public.pem}") Resource publicResource,
            @Value("${app.jwt.private-key:file:certs/jwt-private.pem}") Resource privateResource,
            Environment environment) {
        try {
            if (publicResource.exists() && privateResource.exists()) {
                try (var pub = publicResource.getInputStream(); var priv = privateResource.getInputStream()) {
                    publicKey = RsaKeyConverters.x509().convert(pub);
                    privateKey = RsaKeyConverters.pkcs8().convert(priv);
                }
            } else if (!publicResource.exists() && !privateResource.exists()
                    && environment.acceptsProfiles(Profiles.of("dev", "test"))) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                publicKey = (RSAPublicKey) pair.getPublic();
                privateKey = (RSAPrivateKey) pair.getPrivate();
                if (!environment.acceptsProfiles(Profiles.of("test"))) {
                    var pubPath = publicResource.getFile().toPath();
                    var privPath = privateResource.getFile().toPath();
                    Files.createDirectories(pubPath.toAbsolutePath().getParent());
                    Files.createDirectories(privPath.toAbsolutePath().getParent());
                    Files.writeString(privPath, pem("PRIVATE KEY", privateKey.getEncoded()), StandardOpenOption.CREATE_NEW);
                    Files.writeString(pubPath, pem("PUBLIC KEY", publicKey.getEncoded()), StandardOpenOption.CREATE_NEW);
                }
            } else {
                throw new IllegalStateException("Configure JWT_PUBLIC_KEY and JWT_PRIVATE_KEY with persistent PEM resources.");
            }
            Signature signature = Signature.getInstance("SHA256withRSA");
            byte[] challenge = "coeur-key-validation".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            signature.initSign(privateKey);
            signature.update(challenge);
            byte[] signed = signature.sign();
            signature.initVerify(publicKey);
            signature.update(challenge);
            if (!signature.verify(signed)) {
                throw new IllegalStateException("JWT public and private keys do not match.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load JWT signing keys", e);
        }
    }

    private static String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }

    @Bean
    public RSAPublicKey publicKey() {
        return this.publicKey;
    }

    @Bean
    public RSAPrivateKey privateKey() {
        return this.privateKey;
    }
}
