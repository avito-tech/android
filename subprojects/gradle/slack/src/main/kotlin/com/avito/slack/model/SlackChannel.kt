package com.avito.slack.model

import java.io.Serializable

data class SlackChannel(val name: String) : Serializable {

    init {
        require(name.startsWith("#") || name.startsWith("@")) { "Channel name should start with # or @" }
    }
}

internal val SlackChannel.strippedName: String
    get() = this.name
        .removePrefix("#")
        .removePrefix("@")
