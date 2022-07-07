package com.avito.android.device

import com.avito.android.Result

public interface AndroidDevice {
    public val sdk: Int
    public val type: String
    public val serial: DeviceSerial

    public suspend fun install(application: AndroidApplication): Result<Unit>
    public suspend fun isAlive(): Boolean
    public suspend fun executeInstrumentation(command: InstrumentationCommand): Result<InstrumentationResult>
    public suspend fun clearPackage(appPackage: String): Result<Unit>
    public suspend fun uninstall(app: AndroidApplication): Result<Unit>

    public data class InstrumentationResult(
        val success: Boolean
    )
}
