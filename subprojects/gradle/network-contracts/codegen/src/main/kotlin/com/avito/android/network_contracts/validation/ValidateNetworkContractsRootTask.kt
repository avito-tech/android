package com.avito.android.network_contracts.validation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class ValidateNetworkContractsRootTask : DefaultTask(), BuildVerdictTask {

    @get:Input
    public abstract val projectPath: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val reports: ConfigurableFileCollection

    @get:OutputFile
    public abstract val verdictFile: RegularFileProperty

    @get:Internal
    internal abstract val rootDir: DirectoryProperty

    @get:Internal
    override val verdict: SpannedString
        get() = SpannedString(verdictFile.get().asFile.readText())

    @TaskAction
    public fun validate() {
        val diagnosticsMap = reports
            .filter(File::exists)
            .flatMap { Json.decodeFromStream<List<NetworkContractsDiagnostic>>(it.inputStream()) }
            .groupBy { diagnostic -> diagnostic.issue.key }

        var verdict = OK

        if (diagnosticsMap.isNotEmpty()) {
            verdict = ProblemsMessageBuilder.build(diagnosticsMap)
        }
        verdictFile.get().asFile.writeText(verdict)

        if (verdict != OK) {
            error(verdict)
        }
    }

    internal companion object {

        internal const val NAME = "validateNetworkContracts"
        private const val OK = "OK"
    }
}
