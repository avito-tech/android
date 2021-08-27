package com.avito.test.summary.compose

import com.avito.android.Result
import com.avito.report.model.Team
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.model.CrossDeviceRunTest
import com.avito.test.summary.model.CrossDeviceStatus
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.FailureOnDevice
import com.avito.test.summary.model.createStubInstance
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class SlackSummaryComposerTest {

    private val composer: SlackSummaryComposer = SlackSummaryComposerImpl("http://localhost/")

    @Test
    fun `slack message - contains manual tests count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message.getOrThrow()).contains("Ручные тесты: 2")
    }

    @Test
    fun `slack message - contains automated tests count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains("*Автотесты*: 3")
    }

    @Test
    fun `slack message - contains success tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()

        // не 50% потому что manual не берется в расчет
        assertThat(message.getOrThrow()).contains(
            ":green_heart: *Зеленые тесты*: 3 (60%)"
        )
    }

    @Test
    fun `slack message - contains skipped tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.SkippedOnAllDevices)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains(
            ":white_circle: *Пропущенные тесты (например, заигнорен) на всех девайсах*: 3 (100%)"
        )
    }

    @Test
    fun `slack message - contains lost tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.LostOnSomeDevices),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.LostOnSomeDevices),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.SkippedOnAllDevices)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains(
            ":black_circle: *Потерянные тесты (например, зависли и не зарепортились) на некоторых девайсах*: 2 (50%)"
        )
    }

    @Test
    fun `slack message - contains all failed tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains(":red_circle: *Тесты упали на всех девайсах*: 2 (50%)")
    }

    @Test
    fun `slack message - contains failed tests on some devices count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains(":warning: *Тесты упали только на некоторых девайсах*: 2 (50%)")
    }

    @Test
    fun `slack message - contains failures with count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice("device1", "failure1"),
                            FailureOnDevice("device2", "failure2"),
                            FailureOnDevice("device3", "failure3"),
                            FailureOnDevice("device4", "failure4"),
                            FailureOnDevice("device5", "failure5"),
                            FailureOnDevice("device5", "failure6")
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice("device1", "failure1"),
                            FailureOnDevice("device2", "failure2"),
                            FailureOnDevice("device3", "failure3")
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains("*2* из-за ```failure1```")
        assertThat(message.getOrThrow()).contains("*2* из-за ```failure2```")
        assertThat(message.getOrThrow()).contains("*2* из-за ```failure3```")
        assertThat(message.getOrThrow()).contains("*1* из-за ```failure4```")
        assertThat(message.getOrThrow()).contains("*1* из-за ```failure5```")
        assertThat(message.getOrThrow()).contains("И еще *1*")
    }

    @Test
    fun `slack message - contains @channel - if mention enabled and there are failed tests`() {
        val suiteWithFailure = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice(
                                "device1",
                                "failure1"
                            )
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.Success
                )
            )
        )

        val message = compose(suiteWithFailure, mentionOnFailures = true)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).contains("<!channel>")
    }

    @Test
    fun `slack message - does not contain @channel - if mention enabled and there are no failed tests`() {
        val greenSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success),
                CrossDeviceRunTest.createStubInstance(status = CrossDeviceStatus.Success)
            )
        )

        val message = compose(greenSuite, mentionOnFailures = true)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).doesNotContain("<!channel>")
    }

    @Test
    fun `slack message - does not contain @channel - if mention disabled and there are failed tests`() {
        val suiteWithFailure = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice(
                                "device1",
                                "failure1"
                            )
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = CrossDeviceStatus.Success
                )
            )
        )

        val message = compose(suiteWithFailure, mentionOnFailures = false)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow()).doesNotContain("<!channel>")
    }

    private fun compose(
        testData: CrossDeviceSuite,
        team: Team = Team.UNDEFINED,
        mentionOnFailures: Boolean = false
    ): Result<String> {
        return composer.composeMessage(
            testData = testData,
            team = team,
            mentionOnFailures = mentionOnFailures,
            reportCoordinates = ReportCoordinates("Any", "Any", "Any"),
            reportId = "Any",
            buildUrl = "build_url"
        )
    }
}
