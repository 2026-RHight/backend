@ApplicationModule(
        allowedDependencies = {
            "approval",
            "attendance",
            "core",
            "hr",
            "payroll",
            "performance",
            "email"
        })
package com.reverse.api;

import org.springframework.modulith.ApplicationModule;
