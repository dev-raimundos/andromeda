package br.app.coeur.shared.security;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injeta no parâmetro o ID do usuário autenticado (claim {@code sub} do JWT).
 * O parâmetro deve ser do tipo {@link Long}.
 *
 * <p>É resolvida por {@link CurrentUserIdArgumentResolver}.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(hidden = true) // não aparece como parâmetro de entrada no Swagger
public @interface CurrentUserId {
}
