package br.app.coeur.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtKeyConfig {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyConfig() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Falha ao inicializar o gerador de chaves RSA", e);
        }
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
