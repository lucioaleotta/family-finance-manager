@ApplicationModule(displayName = "Reporting", allowedDependencies = { "transactions", "transactions::api",
                "transactions::domain", "assets", "shared", "shared::domain", "users::security" })
package com.lucio.financeapp.reporting;

import org.springframework.modulith.ApplicationModule;
