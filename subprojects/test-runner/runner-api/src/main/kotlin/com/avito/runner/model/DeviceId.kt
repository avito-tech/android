package com.avito.runner.model

public data class DeviceId(
    val serial: String
) {

    override fun toString(): String = serial

    public companion object
}
