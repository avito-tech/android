package com.avito.emcee.client.internal

import com.avito.emcee.queue.Job
import com.avito.emcee.queue.JobStatusBody
import com.avito.emcee.queue.QueueApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

internal class JobWaiter(
    private val queueApi: QueueApi
) {

    @ExperimentalTime
    suspend fun wait(job: Job, timeout: Duration) {
        withTimeout(timeout) {
            do {
                // TODO handle error
                val status = queueApi.jobStatus(JobStatusBody(id = job.id))
                delay(10.seconds) // TODO how often? to config
            } while (status.queueState.dequeuedBucketCount + status.queueState.enqueuedBucketCount != 0)
        }
    }
}
