package com.avito.instrumentation.configuration.target.scheduling.quota

import java.io.Serializable

open class QuotaConfiguration {

    var retryCount: Int = 0
    var minimumSuccessCount: Int = 0
    var minimumFailedCount: Int = 0

    fun validate() {
        val runsCount = retryCount + 1
        val minimumRequiredRunsCount = minimumSuccessCount + minimumFailedCount

        require(retryCount >= 0) { "Retry count in quota must be positive or 0" }

        require(minimumSuccessCount >= 0) { "minimumSuccessCount must be positive or 0" }
        require(minimumFailedCount >= 0) { "minimumFailedCount must be positive or 0" }
        require(minimumRequiredRunsCount > 0) {
            "minimumRequiredRunsCount (minimumSuccessCount + minimumFailedCount) must be positive"
        }

        require(runsCount >= minimumRequiredRunsCount) {
            "Runs count (retryCount + 1) must be >= minimumSuccessCount + minimumFailedCount"
        }
    }

    fun data() = Data(
        retryCount = retryCount,
        minimumSuccessCount = minimumSuccessCount,
        minimumFailedCount = minimumFailedCount
    )

    data class Data(
        val retryCount: Int,
        val minimumSuccessCount: Int,
        val minimumFailedCount: Int
    ) : Serializable
}
