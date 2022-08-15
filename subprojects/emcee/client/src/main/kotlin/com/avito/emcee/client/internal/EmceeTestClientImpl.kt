package com.avito.emcee.client.internal

import com.avito.emcee.client.EmceeTestClient
import com.avito.emcee.client.EmceeTestClientConfig
import com.avito.emcee.client.internal.result.JobResult
import com.avito.emcee.client.internal.result.JobResultHasFailedTestsException
import com.avito.emcee.client.internal.result.JobResultResolver
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.ScheduleTestsBody
import com.avito.emcee.queue.ScheduledTests
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

internal class EmceeTestClientImpl(
    private val queueApi: QueueApi,
    private val uploader: FileUploader,
    private val testsParser: TestsParser,
    private val waiter: JobWaiter,
    private val jobResultResolver: JobResultResolver,
) : EmceeTestClient {

    @ExperimentalTime
    override fun execute(config: EmceeTestClientConfig) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val testConfigurationFactory = createFactory(config)
                // TODO filter for specific Sdk
                val tests: List<TestEntry> = testsParser.parse(config.testApk).getOrThrow()
                try {
                    config.devices.map { device ->
                        async {
                            queueApi.scheduleTests(
                                createBody(
                                    config,
                                    testConfigurationFactory.create(device),
                                    tests
                                )
                            )
                        }
                    }.forEach {
                        it.await()
                    }
                } catch (e: Throwable) {
                    // TODO clean up successfully scheduled tests
                    throw e
                }
                waiter.waitJobIsDone(config.job, 60.minutes)

                val result = jobResultResolver.resolveResult(config.job)
                if (result is JobResult.Failure) throw JobResultHasFailedTestsException(result.failedTests)
            }
        }
    }

    private fun createBody(
        config: EmceeTestClientConfig,
        configuration: TestConfiguration,
        tests: List<TestEntry>
    ): ScheduleTestsBody {
        return ScheduleTestsBody(
            prioritizedJob = config.job,
            scheduleStrategy = config.scheduleStrategy,
            tests = ScheduledTests(
                config = ScheduledTests.Config(configuration),
                testEntries = tests
            )
        )
    }

    private suspend fun createFactory(
        config: EmceeTestClientConfig
    ): TestConfigurationFactory =
        withContext(Dispatchers.IO) {
            val apkUrl = async { uploader.upload(config.apk) }
            val testApkUrl = async { uploader.upload(config.testApk) }
            TestConfigurationFactory(
                apkUrl = apkUrl.await(),
                testApkUrl = testApkUrl.await(),
                testMaximumDurationSec = config.testMaximumDurationSec,
                testExecutionBehavior = config.testExecutionBehavior,
                appPackage = config.appPackage,
                testAppPackage = config.testAppPackage,
                testRunnerClass = config.testRunnerClass,
            )
        }
}
