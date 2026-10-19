package br.app.coeur;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    private static final ApplicationModules MODULES = ApplicationModules.of(ApiApplication.class);

    @Test
    void shouldRespectModuleBoundaries() {
        MODULES.verify();
    }

    @Test
    void shouldPrintModuleStructure() {
        System.out.println(MODULES);
    }
}
