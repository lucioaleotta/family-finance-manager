@ApplicationModule(displayName = "Assets", allowedDependencies = { "shared", "shared::domain", "transactions",
                "transactions::domain", "transactions::api", "transactions::application", "config", "shared::fx",
                "users::security", "transactions::ports" })
package com.lucio.financeapp.assets;

import org.springframework.modulith.ApplicationModule;
