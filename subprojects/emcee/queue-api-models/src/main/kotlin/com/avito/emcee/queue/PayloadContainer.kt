package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class PayloadContainer(
    val payload: Payload,
    val payloadType: String,
)
