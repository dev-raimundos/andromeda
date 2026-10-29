package br.app.coeur.core.config.jwt;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import java.util.UUID;

record JwtKeyMaterial(UUID id, JwtEncoder encoder, JwtDecoder decoder) {
}
