package br.app.coeur.shared.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

    public String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
