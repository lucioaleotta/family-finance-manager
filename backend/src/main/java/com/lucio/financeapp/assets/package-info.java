@ApplicationModule(displayName = "Assets", allowedDependencies = { "shared", "shared::domain", "transactions",
        "transactions::domain", "transactions::api", "transactions::application", "config" })
package com.lucio.financeapp.assets;

import org.springframework.modulith.ApplicationModule;
