package com.avito.runner.scheduler.listener

import com.avito.logger.Logger
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    private val logger: Logger
) : TestListener {

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        lifecycleListener.started(
            test = test,
            device = device,
            executionNumber = executionNumber
        )
    }

    @ExperimentalPathApi
    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val tempDirectory = createTempDirectory()
        val testMetadataDirectory = testMetadataFullDirectory(
            targetPackage = targetPackage,
            test = test
        )

        try {
            val pullingResult = device.pull(
                from = testMetadataDirectory.toPath(),
                to = tempDirectory
            )

            val resultDirectory = pullingResult
                .map {
                    File(
                        tempDirectory.toFile(),
                        testMetadataDirectoryPath(test)
                    )
                }

            lifecycleListener.finished(
                artifacts = resultDirectory,
                test = test,
                executionNumber = executionNumber
            )

            device.clearDirectory(
                remotePath = testMetadataDirectory.toPath()
            ).invoke()
        } catch (t: Throwable) {
            logger.warn("Failed to process artifacts from $device", t)
        } finally {
            tempDirectory.toFile().delete()
        }
    }

    private fun testMetadataFullDirectory(targetPackage: String, test: TestCase): File =
        File("/sdcard/Android/data/$targetPackage/files/runner/${testMetadataDirectoryPath(test)}")

    private fun testMetadataDirectoryPath(test: TestCase): String = "${test.className}#${test.methodName}"
}
