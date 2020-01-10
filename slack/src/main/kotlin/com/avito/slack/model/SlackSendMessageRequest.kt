package com.avito.slack.model

data class SlackSendMessageRequest(
    val channel: SlackChannel,
    val text: String,
    val author: String,
    val emoji: String? = null,
    val threadId: String? = null
) {

    override fun toString(): String {
        return "Message "
    }
}
