package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask.Companion.NAME
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic

internal object ProblemsMessageBuilder {

    fun build(diagnostics: Map<String, List<NetworkContractsDiagnostic>>): String {
        return buildString {
            appendLine("Validation of the network contracts plugin failed:")

            diagnostics.forEach { (key, diagnosticsList) ->
                appendLine("- $key")
                diagnosticsList.forEach { diagnostic ->
                    appendLine("\t- ${diagnostic.message}")
                }
            }

            appendLine()
            appendLine("You can locally run validation task:")
            appendLine("`./gradlew $NAME`")
        }
    }
}
