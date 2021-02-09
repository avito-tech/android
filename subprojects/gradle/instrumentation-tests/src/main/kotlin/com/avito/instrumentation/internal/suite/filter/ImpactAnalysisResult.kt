package com.avito.instrumentation.internal.suite.filter

import com.avito.instrumentation.configuration.ImpactAnalysisPolicy
import java.io.File

public data class ImpactAnalysisResult(
    val policy: ImpactAnalysisPolicy,
    val affectedTests: List<String>,
    val addedTests: List<String>,
    val modifiedTests: List<String>,
    val changedTests: List<String>
) {

    public companion object {
        public fun create(
            policy: ImpactAnalysisPolicy,
            affectedTestsFile: File?,
            addedTestsFile: File?,
            modifiedTestsFile: File?,
            changedTestsFile: File?
        ): ImpactAnalysisResult = ImpactAnalysisResult(
            policy = policy,
            affectedTests = parseFile(affectedTestsFile),
            addedTests = parseFile(addedTestsFile),
            modifiedTests = parseFile(modifiedTestsFile),
            changedTests = parseFile(changedTestsFile)
        )

        private fun parseFile(file: File?): List<String> {
            return if (file != null && file.exists()) {
                file.readLines()
            } else {
                emptyList()
            }
        }
    }
}
