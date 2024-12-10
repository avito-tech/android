package com.avito.android.network_contracts.validation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.network_contracts.validation.analyzer.NetworkContractsProblemsAnalyzer
import com.avito.android.network_contracts.validation.analyzer.rules.NetworkContractsDiagnosticRule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class ValidateNetworkContractsTask : DefaultTask(), BuildVerdictTask {

    @get:Input
    public abstract val failFast: Property<Boolean>

    @get:OutputFile
    public abstract val resultFile: RegularFileProperty

    @get:OutputFile
    public abstract val verdictFile: RegularFileProperty

    @get:Internal
    override val verdict: SpannedString
        get() = SpannedString(verdictFile.get().asFile.readText())

    internal abstract fun createRules(): List<NetworkContractsDiagnosticRule>

    @TaskAction
    public fun validate() {
        val networkContractsProblemsAnalyzer = NetworkContractsProblemsAnalyzer(createRules())

        val validationDetections = networkContractsProblemsAnalyzer.analyze()
        if (failFast.get() && validationDetections.isNotEmpty()) {
            val diagnostics = validationDetections.groupBy { diagnostic -> diagnostic.issue.key }
            val verdict = ProblemsMessageBuilder.build(diagnostics)
            verdictFile.get().asFile.writeText(verdict)
            error(verdict)
        } else {
            resultFile.get().asFile.writeText(Json.encodeToString(validationDetections))
        }
    }
}
