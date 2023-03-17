package com.avito.emcee.worker

import com.avito.emcee.queue.workercapability.WorkerCapability
import com.avito.emcee.queue.workercapability.defaultCapabilities
import com.avito.emcee.worker.configuration.PayloadSignature
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class GetBucketBody(
    val workerId: String,
    val payloadSignature: PayloadSignature,
    val workerCapabilities: List<WorkerCapability> = defaultCapabilities(),
)
