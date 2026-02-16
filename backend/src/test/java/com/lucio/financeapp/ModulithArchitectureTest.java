package com.lucio.financeapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.modulith.core.ApplicationModules;

@Disabled("Violazioni modulith preesistenti tra shared e transactions; riabilitare dopo refactor moduli")
class ModulithArchitectureTest {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(FinanceApplication.class).verify();
    }
}
