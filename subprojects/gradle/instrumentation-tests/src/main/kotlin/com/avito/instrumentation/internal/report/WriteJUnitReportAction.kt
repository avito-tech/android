package com.avito.instrumentation.internal.report

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.Stability
import com.avito.report.model.Status
import com.avito.report.model.TestName
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class WriteJUnitReportAction(
    private val testSuiteNameProvider: TestSuiteNameProvider,
    private val reportLinkGenerator: ReportLinkGenerator,
    private val destination: File
) : FinalizeAction {

    private val estimatedTestRecordSize = 150

    override fun action(testRunResult: TestRunResult) {
        val testCountOverall = testRunResult.testCount()
        val testCountSuccess = testRunResult.successCount()
        val testCountFailures = testRunResult.failureCount()
        val testCountErrors = testRunResult.notReportedCount()
        val testCountSkipped = testRunResult.skippedCount()

        require(testCountOverall == testCountSuccess + testCountFailures + testCountSkipped + testCountErrors)

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${testSuiteNameProvider.getName()}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""errors="$testCountErrors" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="${testRunResult.testsDuration}" """)
            appendLine(">")

            appendLine("<properties/>")

            testRunResult.reportedTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.className}" """)
                append("""name="${test.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="${test.lastAttemptDurationInSeconds}"""")
                appendLine(">")

                if (test.stability is Stability.Flaky) {
                    appendLine("<system-out>")
                    appendEscapedLine(
                        "Flaky test. " +
                            "Success runs: ${(test.stability as Stability.Flaky).successCount} " +
                            "out of ${(test.stability as Stability.Flaky).attemptsCount}"
                    )
                    appendLine("</system-out>")
                }

                when (test.status) {
                    is Status.Skipped -> {
                        appendLine("<skipped/>")
                        if (test.skipReason != null) {
                            appendLine("<system-out>")
                            appendEscapedLine("Тест не запускался: ${test.skipReason}")
                            appendLine("</system-out>")
                        }
                    }
                    is Status.Failure -> {
                        appendLine("<failure>")
                        appendEscapedLine((test.status as Status.Failure).verdict)
                        appendLine(reportLinkGenerator.generateTestLink(TestName(test.className, test.methodName)))
                        appendLine("</failure>")
                    }
                    is Status.Lost -> {
                        appendLine("<error>")
                        appendLine("LOST (no info in report)")
                        appendLine(reportLinkGenerator.generateTestLink(TestName(test.className, test.methodName)))
                        appendLine("</error>")
                    }
                    Status.Success -> { /* do nothing */
                    }
                    Status.Manual -> { /* do nothing */
                    }
                }

                appendLine("</testcase>")
            }

            testRunResult.notReported.lostTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.name.className}" """)
                append("""name="${test.name.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="unknown"""")
                appendLine(">")

                appendLine("<error>")
                appendLine("Not reported ${reportLinkGenerator.generateTestLink(test.name)}")
                appendLine("</error>")

                appendLine("</testcase>")
            }

            appendLine("</testsuite>")
        }

        destination.writeText(xml)
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendLine(StringEscapeUtils.escapeXml10(line))
    }
}
