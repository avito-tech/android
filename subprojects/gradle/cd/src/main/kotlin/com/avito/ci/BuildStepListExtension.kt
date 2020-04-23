package com.avito.ci

import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.BuildStep
import com.avito.ci.steps.CompileUiTests
import com.avito.ci.steps.ConfigurationCheck
import com.avito.ci.steps.ImpactAnalysisAwareBuildStep
import com.avito.ci.steps.LintCheck
import com.avito.ci.steps.PerformanceTestCheck
import com.avito.ci.steps.UiTestCheck
import com.avito.ci.steps.UnitTestCheck
import com.avito.ci.steps.UploadBuildResult
import com.avito.ci.steps.UploadToArtifactory
import com.avito.ci.steps.UploadToProsector
import com.avito.ci.steps.UploadToQapps
import com.avito.ci.steps.VerifyArtifactsStep
import groovy.lang.Closure
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty


@Suppress("UnstableApiUsage")
open class BuildStepListExtension(
    protected val name: String,
    objects: ObjectFactory
) {

    protected val artifactsConfig = ArtifactsConfiguration()

    internal val steps: ListProperty<BuildStep> = objects.listProperty(BuildStep::class.java).empty()

    internal var useImpactAnalysis: Boolean = false

    fun configuration(closure: Closure<ConfigurationCheck>) {
        configureAndAdd(ConfigurationCheck(name), closure)
    }

    fun uiTests(closure: Closure<UiTestCheck>) {
        configureAndAdd(UiTestCheck(name), closure)
    }

    fun performanceTests(closure: Closure<PerformanceTestCheck>) {
        configureAndAdd(PerformanceTestCheck(name), closure)
    }

    fun compileUiTests(closure: Closure<CompileUiTests>) {
        configureAndAdd(CompileUiTests(name), closure)
    }

    fun unitTests(closure: Closure<UnitTestCheck>) {
        configureAndAdd(UnitTestCheck(name), closure)
    }

    fun lint(closure: Closure<LintCheck>) {
        configureAndAdd(LintCheck(name), closure)
    }

    fun uploadToQapps(closure: Closure<UploadToQapps>) {
        configureAndAdd(UploadToQapps(name, artifactsConfig), closure)
    }

    fun uploadToArtifactory(closure: Closure<UploadToArtifactory>) {
        configureAndAdd(UploadToArtifactory(name, artifactsConfig), closure)
    }

    fun uploadToProsector(closure: Closure<UploadToProsector>) {
        configureAndAdd(UploadToProsector(name, artifactsConfig), closure)
    }

    fun uploadBuildResult(closure: Closure<UploadBuildResult>) {
        configureAndAdd(UploadBuildResult(name), closure)
    }

    fun artifacts(closure: Closure<ArtifactsConfiguration>) {
        val step = VerifyArtifactsStep(name, artifactsConfig)
        steps.add(step)

        closure.delegate = artifactsConfig
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        step.useImpactAnalysis = this.useImpactAnalysis
    }

    protected fun <T : BuildStep> configureAndAdd(step: T, configure: Closure<T>) {
        configure.delegate = step
        configure.resolveStrategy = Closure.DELEGATE_FIRST
        configure.call()
        if (step is ImpactAnalysisAwareBuildStep) {
            step.useImpactAnalysis = this.useImpactAnalysis
        }
        steps.add(step)
    }
}
