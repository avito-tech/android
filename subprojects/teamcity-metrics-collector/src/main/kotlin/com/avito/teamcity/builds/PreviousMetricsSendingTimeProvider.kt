package com.avito.teamcity.builds

import com.avito.teamcity.TeamcityApi
import org.jetbrains.teamcity.rest.ProjectId
import java.time.Instant

internal interface PreviousMetricsSendingTimeProvider {
    fun getPreviousSendingTime(): Instant
    fun saveSendingTime(time: Instant)

    companion object {
        private const val PROJECT_ID = "AndroidProjects_Tools"
        private const val PROJECT_PARAMETER_KEY = "previousMetricsSendingTime"

        fun create(api: TeamcityApi): PreviousMetricsSendingTimeProvider = Impl(api)
    }

    private class Impl(
        api: TeamcityApi,
    ) : PreviousMetricsSendingTimeProvider {
        val project = api.getProjectById(ProjectId(PROJECT_ID))

        override fun saveSendingTime(time: Instant) {
            project.setParameter(PROJECT_PARAMETER_KEY, time.toString())
        }

        override fun getPreviousSendingTime(): Instant {
            val previousSendingTime = project.parameters.find { it.name == PROJECT_PARAMETER_KEY }
            require(previousSendingTime != null && previousSendingTime.value.isNotBlank()) {
                """
                |Setup $PROJECT_ID. Add $PROJECT_PARAMETER_KEY configuration parameter.
                |Current value: ${previousSendingTime?.value}
                |Example: ${Instant.now()}""".trimMargin()
            }
            return Instant.parse(previousSendingTime.value)
        }
    }
}
