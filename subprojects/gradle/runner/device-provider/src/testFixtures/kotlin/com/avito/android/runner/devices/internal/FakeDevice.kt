package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.runner.service.worker.device.Serial
import java.io.File

internal open class FakeDevice(
    override val serial: Serial
) : Device {

    var waitForBoot: suspend () -> Boolean = { true }

    override fun redirectLogcatToFile(file: File, tags: Collection<String>) {
        // empty
    }

    override suspend fun waitForBoot(): Boolean {
        return this.waitForBoot.invoke()
    }
}

internal class FakeRemoteDevice(
    override val serial: Serial.Remote
) : FakeDevice(serial), RemoteDevice {

    override fun disconnect() = Result.Success("stub-disconnect")

    override fun connect() = Result.Success("stub-connect")
}
