package com.avito.android.runner.devices.internal

import com.avito.k8s.model.KubePod
import com.avito.runner.service.worker.device.Serial
import java.io.File

internal class EmulatorsLogsReporterImpl(
    private val outputFolder: File,
    private val logcatDir: File,
    private val logcatTags: Collection<String>,
    private val disableLogcat: Boolean,
) : EmulatorsLogsReporter {

    override fun reportEmulatorLogs(pod: KubePod, emulatorName: Serial, log: String) {
        val logFile = getLogFile(emulatorName.value)
        logFile.parentFile?.mkdirs()
        logFile.appendText("--- Logs of emulator: $pod ---\n")
        logFile.appendText("$log\n")
    }

    override fun redirectLogcat(emulatorName: Serial, device: Device) {
        val logcatFile = getFile(
            dir = logcatDir,
            emulatorName = emulatorName.value
        )

        if (disableLogcat) {
            logcatFile.writeText("Logcat is disabled by Experiments")
            return
        }

        device.redirectLogcatToFile(
            file = logcatFile,
            tags = logcatTags
        )
    }

    override fun getLogFile(podIp: String): File {
        return File(File(outputFolder, DEVICES_LOGS), "$podIp.txt")
    }

    private fun getFile(
        dir: File,
        emulatorName: String
    ) = File(dir, "$emulatorName.txt").apply { parentFile?.mkdirs() }
}

private const val DEVICES_LOGS = "devices"
