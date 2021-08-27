package com.avito.test.summary.model

import com.avito.math.Percent
import com.avito.math.fromZeroToOnePercent
import com.avito.math.percentOf
import com.avito.report.model.Team
import com.avito.reportviewer.model.team

public data class CrossDeviceSuite(val crossDeviceRuns: List<CrossDeviceRunTest>) {

    private val consistentData: List<CrossDeviceRunTest>
        get() = crossDeviceRuns.filter { it.status !is CrossDeviceStatus.Inconsistent }

    val manualCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.Manual }

    val automatedCount: Int
        get() = consistentData.count { it.status !is CrossDeviceStatus.Manual }

    val success: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.Success }

    val skippedOnAllDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.SkippedOnAllDevices }

    val lostOnSomeDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.LostOnSomeDevices }

    val failedOnAllDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnAllDevices }

    val failedOnSomeDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnSomeDevices }

    val percentLostOnSomeDevicesOfAutomated: Percent
    val percentSuccessOfAutomated: Percent
    val percentSkippedOnAllDevicesOfAutomated: Percent
    val percentFailedOnAllDevicesOfAutomated: Percent
    val percentFailedOnSomeDevicesOfAutomated: Percent

    init {
        if (automatedCount == 0) {
            val zeroPercents = 0F.fromZeroToOnePercent()
            percentSuccessOfAutomated = zeroPercents
            percentSkippedOnAllDevicesOfAutomated = zeroPercents
            percentFailedOnAllDevicesOfAutomated = zeroPercents
            percentFailedOnSomeDevicesOfAutomated = zeroPercents
            percentLostOnSomeDevicesOfAutomated = zeroPercents
        } else {
            percentSuccessOfAutomated = success.percentOf(automatedCount)
            percentSkippedOnAllDevicesOfAutomated = skippedOnAllDevicesCount.percentOf(automatedCount)
            percentFailedOnAllDevicesOfAutomated = failedOnAllDevicesCount.percentOf(automatedCount)
            percentFailedOnSomeDevicesOfAutomated = failedOnSomeDevicesCount.percentOf(automatedCount)
            percentLostOnSomeDevicesOfAutomated = lostOnSomeDevicesCount.percentOf(automatedCount)
        }
    }

    public fun filterTeam(team: Team): CrossDeviceSuite =
        CrossDeviceSuite(crossDeviceRuns.filter { it.name.team == team })
}
