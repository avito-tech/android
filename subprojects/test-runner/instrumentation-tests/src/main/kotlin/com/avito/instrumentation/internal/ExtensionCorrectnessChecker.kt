package com.avito.instrumentation.internal

import com.android.build.api.variant.TestVariant
import com.android.build.api.variant.Variant
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension

internal class ExtensionCorrectnessChecker(
    private val extension: InstrumentationTestsPluginExtension
) {
    internal fun check(variant: Variant) {
        val isTestVariantPropertiesConfigured = extension.macrobenchmark.isAnyPropertyConfigured()
        if (isTestVariantPropertiesConfigured) {
            check(variant is TestVariant) {
                "Instrumentation plugin's macrobenchmark section is declared, but cannot be applied. " +
                    "Macrobenchmark properties only make sense when using " +
                    "Android Test Variant (`com.android.test` plugin)."
            }
        }
    }
}
