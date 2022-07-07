package com.avito.instrumentation.internal

import com.android.build.api.dsl.CommonExtension
import com.avito.android.plugins.configuration.BuildEnvResolver
import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.kotlin.dsl.filterNotBlankValues
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.runner.config.InstrumentationParameters
import org.gradle.api.Project

/**
 * Instrumentation args consumed in `parseEnvironment()`
 *
 * TODO: Make stronger contract: MBS-7890
 */
internal class InstrumentationArgsResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val sentryResolver: SentryResolver,
    private val reportResolver: ReportResolver,
    private val planSlugResolver: PlanSlugResolver,
    private val runIdResolver: RunIdResolver,
    private val buildEnvResolver: BuildEnvResolver,
    private val androidDslInteractor: AndroidDslInteractor,
) {

    private val pluginLevelInstrumentationArgs: MutableMap<String, String> = mutableMapOf()

    /**
     * These params is set in InstrumentationPluginExtension, or available as project properties
     * Will be resolved early in configuration even if test task is not in execution graph
     * These params will be set as instrumentation args for local run
     */
    fun resolvePluginLevelArgs(project: Project, androidExtension: CommonExtension<*, *, *, *>): Map<String, String> {
        if (pluginLevelInstrumentationArgs.isEmpty()) {
            val argsFromDsl = mutableMapOf<String, String>()

            argsFromDsl.putAll(filterNotBlankValues(androidDslInteractor.getInstrumentationArgs(androidExtension)))

            pluginLevelInstrumentationArgs.consumeArg("planSlug", argsFromDsl) {
                planSlugResolver.generateDefaultPlanSlug(project.path)
            }
            pluginLevelInstrumentationArgs.consumeArg("jobSlug", argsFromDsl) {
                "LocalTests" // todo could be configuration name
            }
            pluginLevelInstrumentationArgs.consumeArg("avito.report.enabled", argsFromDsl) {
                project.getBooleanProperty("avito.report.enabled", default = false).toString()
            }
            pluginLevelInstrumentationArgs.consumeArg("fileStorageUrl", argsFromDsl) {
                reportResolver.getFileStorageUrl()
            }
            pluginLevelInstrumentationArgs.consumeArg("sentryDsn", argsFromDsl) {
                sentryResolver.getSentryDsn().orNull
            }
            pluginLevelInstrumentationArgs.consumeArg("deviceName", argsFromDsl) {
                "local"
            }
            pluginLevelInstrumentationArgs.consumeArg("reportApiUrl", argsFromDsl) {
                reportResolver.getReportApiUrl()
            }
            pluginLevelInstrumentationArgs.consumeArg("reportViewerUrl", argsFromDsl) {
                reportResolver.getReportViewerUrl()
            }

            // runId from dsl will be ignored
            pluginLevelInstrumentationArgs["runId"] = runIdResolver.getLocalRunId().toReportViewerFormat()
            argsFromDsl.remove("runId")

            // put everything that left in args 'as is'
            pluginLevelInstrumentationArgs.putAll(argsFromDsl)
        }

        return pluginLevelInstrumentationArgs
    }

    fun getInstrumentationArgsForTestTask(): InstrumentationParameters {
        require(pluginLevelInstrumentationArgs.isNotEmpty()) {
            "Error: pluginLevelInstrumentationArgs is empty\n" +
                "resolvePluginLevelArgs() should be called before\n" +
                "it is that way, because new AGP API doesn't have dsl values available in onVariants API\n" +
                "so we should extract and store it somewhere"
        }

        return InstrumentationParameters()
            .applyParameters(pluginLevelInstrumentationArgs)
            .applyParameters(extension.instrumentationParams)
            .applyParameters(resolveLateArgs())
    }

    /**
     * Postponed resolution of args, which is unavailable locally
     * Local runs of test task without these parameters set is not supported yet
     */
    private fun resolveLateArgs(): Map<String, String> {
        val args = mutableMapOf<String, String>()

        // overwrites runId, because previous value only used for local runs
        args["runId"] = reportResolver.getRunId()
        args["teamcityBuildId"] = buildEnvResolver.getBuildId()
        return args
    }

    private fun MutableMap<String, String>.consumeArg(
        key: String,
        argsFromDsl: MutableMap<String, String>,
        valueFromExtension: () -> String?
    ) {
        val finalValue: String? = if (argsFromDsl.containsKey(key)) {
            val valueFromScript = argsFromDsl[key]
            if (valueFromScript.isNullOrBlank()) {
                valueFromExtension.invoke()
            } else {
                valueFromScript
            }
        } else {
            valueFromExtension.invoke()
        }
        if (!finalValue.isNullOrBlank()) {
            put(key, finalValue)
        }
        argsFromDsl.remove(key)
    }
}
