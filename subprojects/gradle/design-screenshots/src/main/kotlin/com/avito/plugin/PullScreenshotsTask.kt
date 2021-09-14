package com.avito.plugin

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.time.DefaultTimeProvider
import com.avito.utils.ProcessRunner
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

public abstract class PullScreenshotsTask : DefaultTask() {

    @get:Input
    public abstract val applicationIdProperty: Property<String>

    @get:Internal
    public abstract val loggerFactory: Property<LoggerFactory>

    @TaskAction
    public fun pullScreenshots() {
        val loggerFactory = loggerFactory.get()
        val logger = loggerFactory.create<PullScreenshotsTask>()

        val applicationId = applicationIdProperty.get()
        val adb = Adb()
        val adbDevicesManager = AdbDevicesManager(loggerFactory = loggerFactory, adb = adb)
        val currentDevice = DeviceProviderLocal(
            adbDevicesManager = adbDevicesManager,
            adbDeviceFactory = AdbDeviceFactory(
                loggerFactory = loggerFactory,
                adb = adb,
                timeProvider = DefaultTimeProvider(),
                metricsConfig = null,
                processRunner = ProcessRunner.create(null)
            )
        ).getDevice()

        val referencePath = Paths.get("${project.projectDir.path}/src/androidTest/assets/screenshots/")
        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")

        currentDevice.list(remotePath.toString()).fold(
            onSuccess = { result ->
                result.firstOrNull { it.trim().isNotEmpty() }
                    ?.let { directory ->
                        if (directory.trim().isNotEmpty()) {
                            referencePath.toFile().mkdirs()
                            val remoteEmulatorPath = remotePath.resolve(directory)
                            currentDevice.pull(
                                from = remoteEmulatorPath,
                                to = referencePath
                            )
                        }
                    }
                logger.debug("Screenshots are pulled to $referencePath")
                clearOutputFiles(logger)
            },
            onFailure = {
                logger.warn("Cannot list screenshot directory", it)
            }
        )
    }

    private fun clearOutputFiles(logger: Logger) {
        File(project.rootDir.path).listFiles()?.forEach { file ->
            if (!file.isDirectory && file.name.endsWith(".output")) {
                file.delete()
            }
        }
        logger.info("Cleared .output files")
    }
}
