package com.avito.runner.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.Flakiness
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.test.model.DeviceName
import com.avito.test.model.TestName
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider
import com.avito.truth.assertThat
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class VerdictDeterminerImplTest {

    @Test
    fun `verdict - success - empty test suite`() {
        val verdictDeterminer = createVerdictDeterminer()

        val verdict = verdictDeterminer.determine(
            initialTestSuite = emptySet(),
            testResults = emptyList()
        )

        assertThat(verdict).isInstanceOf<Verdict.Success.OK>()
    }

    @Test
    fun `verdict - lost - test results doesn't contain test from initial suite`() {
        val verdictDeterminer = createVerdictDeterminer()

        val lostTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val executedTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test2", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(
                lostTest,
                executedTest
            ),
            testResults = listOf(
                createTestExecution(executedTest)
            )
        )

        assertThat<Verdict.Failure>(verdict) {
            assertThat(notReportedTests).contains(lostTest)
        }
    }

    /**
     * we should consider to fail on this case
     */
    @Test
    fun `verdict - lost - test results contains more tests than initial suite`() {
        val verdictDeterminer = createVerdictDeterminer()

        val extraTest = createTestExecution(
            TestStaticDataPackage.createStubInstance(
                name = TestName("com.test.Test2", "test"),
                deviceName = DeviceName("DEVICE")
            )
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(
                TestStaticDataPackage.createStubInstance(
                    name = TestName("com.test.Test1", "test"),
                    deviceName = DeviceName("DEVICE")
                )
            ),
            testResults = listOf(
                createTestExecution(
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.test.Test1", "test"),
                        deviceName = DeviceName("DEVICE")
                    )
                ),
                extraTest
            )
        )

        assertThat(verdict).isInstanceOf<Verdict.Success.OK>()
    }

    @Test
    fun `verdict - success - single success test`() {
        val verdictDeterminer = createVerdictDeterminer()

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createSuccessTestExecution(testStaticData = test)
            )
        )

        assertThat(verdict).isInstanceOf<Verdict.Success.OK>()
    }

    @Test
    fun `verdict - fail - single failed test`() {
        val verdictDeterminer = createVerdictDeterminer(
            suppressFlaky = false,
            suppressFailure = false
        )

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createFailedTestExecution(testStaticData = test)
            )
        )

        assertThat<Verdict.Failure>(verdict) {
            assertThat(unsuppressedFailedTests).contains(test)
        }
    }

    @Test
    fun `verdict - suppressed - single failed test with suppress failure`() {
        val verdictDeterminer = createVerdictDeterminer(
            suppressFlaky = false,
            suppressFailure = true
        )

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createFailedTestExecution(testStaticData = test)
            )
        )

        assertThat(verdict).isInstanceOf<Verdict.Success.Suppressed>()
    }

    @Test
    fun `verdict - suppressed - single failed flaky test with suppress flaky`() {
        val verdictDeterminer = createVerdictDeterminer(
            suppressFlaky = true,
            suppressFailure = false
        )

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE"),
            flakiness = Flakiness.Flaky("flaky")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createFailedTestExecution(testStaticData = test)
            )
        )

        assertThat(verdict).isInstanceOf<Verdict.Success.Suppressed>()
    }

    @Test
    fun `verdict - failed - single failed stable test with suppress flaky`() {
        val verdictDeterminer = createVerdictDeterminer(
            suppressFlaky = true,
            suppressFailure = false
        )

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE"),
            flakiness = Flakiness.Stable
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createFailedTestExecution(testStaticData = test)
            )
        )

        assertThat<Verdict.Failure>(verdict) {
            assertThat(unsuppressedFailedTests).contains(test)
        }
    }

    @Test
    fun `verdict - failed with unsuppressedFailedTest - single lost(infra error) test`() {
        val verdictDeterminer = createVerdictDeterminer()

        val test = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(test),
            testResults = listOf(
                createLostTestExecution(testStaticData = test)
            )
        )

        assertThat<Verdict.Failure>(verdict) {
            assertThat(unsuppressedFailedTests).contains(test)
        }
    }

    @Test
    fun `verdict - failed with unsuppressedFailedTests - lost(infra error) and failed reported and fails suppressed`() {
        val verdictDeterminer = createVerdictDeterminer(suppressFailure = true)

        val lostTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val failedTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test2", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val results = listOf(
            createLostTestExecution(testStaticData = lostTest),
            createFailedTestExecution(testStaticData = failedTest)
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(
                lostTest,
                failedTest
            ),
            testResults = results
        )

        assertThat<Verdict.Success.Suppressed>(verdict) {
            assertThat(failedTests).containsExactlyElementsIn(results)
        }
    }

    @Test
    fun `verdict - failed - lost(infra error) and failed reported`() {
        val verdictDeterminer = createVerdictDeterminer(suppressFailure = false)

        val lostTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test1", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val failedTest = TestStaticDataPackage.createStubInstance(
            name = TestName("com.test.Test2", "test"),
            deviceName = DeviceName("DEVICE")
        )

        val results = listOf(
            createLostTestExecution(testStaticData = lostTest),
            createFailedTestExecution(testStaticData = failedTest)
        )

        val verdict = verdictDeterminer.determine(
            initialTestSuite = setOf(
                lostTest,
                failedTest
            ),
            testResults = results
        )

        assertThat<Verdict.Failure>(verdict) {
            assertThat(unsuppressedFailedTests).containsExactlyElementsIn(results)
        }
    }

    private fun createLostTestExecution(
        testStaticData: TestStaticData
    ) = AndroidTest.Lost.createStubInstance(testStaticData)

    private fun createFailedTestExecution(
        testStaticData: TestStaticData,
        incident: Incident = Incident.createStubInstance()
    ) = createTestExecution(
        testStaticData = testStaticData,
        testRuntimeData = TestRuntimeDataPackage.createStubInstance(incident = incident)
    )

    private fun createSuccessTestExecution(
        testStaticData: TestStaticData
    ) = createTestExecution(
        testStaticData = testStaticData
    )

    private fun createTestExecution(
        testStaticData: TestStaticData,
        testRuntimeData: TestRuntimeData = TestRuntimeDataPackage.createStubInstance()
    ) = AndroidTest.Completed.createStubInstance(
        testStaticData = testStaticData,
        testRuntimeData = testRuntimeData
    )

    private fun createVerdictDeterminer(
        suppressFlaky: Boolean = false,
        suppressFailure: Boolean = false,
        timeProvider: TimeProvider = StubTimeProvider(),
    ): VerdictDeterminer {
        return VerdictDeterminerImpl(
            suppressFlaky = suppressFlaky,
            suppressFailure = suppressFailure,
            timeProvider = timeProvider,
        )
    }
}
