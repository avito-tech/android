package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.report.model.DeviceName
import java.io.Serializable

interface TestsFilter {

    sealed class Result {
        object Included : Result()
        abstract class Excluded(
            val byFilter: String,
            val reason: String
        ) : Result() {
            class HaveSkipSdkAnnotation(name: String, sdk: Int) : Excluded(
                name, "test has SkipSdk with value sdk=$sdk"
            )

            class DoNotHaveIncludeAnnotations(name: String, annotations: Set<String>) :
                Excluded(name, "test doesn't have any of annotations=$annotations")

            class HaveExcludeAnnotations(name: String, annotations: Set<String>) :
                Excluded(name, "test has any of excluded annotations=$annotations")

            abstract class BySignatures(name: String, reason: String) : Excluded(name, reason) {
                abstract val source: Signatures.Source
            }

            class DoNotMatchIncludeSignature(
                name: String,
                override val source: Signatures.Source
            ) : BySignatures(name, "test doesn't match any of signatures from source=$source")

            class MatchExcludeSignature(
                name: String,
                override val source: Signatures.Source
            ) : BySignatures(name, "test has matched one of signatures from source=$source")
        }
    }

    data class Test(
        val name: String,
        val annotations: List<AnnotationData>,
        val deviceName: DeviceName,
        val api: Int
    ) {
        companion object
    }

    data class Signatures(
        val source: Source,
        val signatures: Filter.Value<TestSignature>
    ) {
        enum class Source {
            Code, ImpactAnalysis, PreviousRun, Report
        }

        data class TestSignature(
            val name: String,
            val deviceName: String? = null
        ) : Serializable
    }

    val name: String

    fun filter(test: Test): Result
}