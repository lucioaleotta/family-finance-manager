package com.lucio.financeapp;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(FinanceApplication.class).verify();
    }
}
