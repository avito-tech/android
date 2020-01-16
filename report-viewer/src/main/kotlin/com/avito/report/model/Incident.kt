package com.avito.report.model

import com.google.gson.annotations.SerializedName

data class Incident(
    @SerializedName("type") val type: Type,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("trace") val trace: List<String>,
    @SerializedName("chain") val chain: List<IncidentElement>,
    @SerializedName("entry_list") val entryList: List<Entry>
) {
    enum class Type {

        @SerializedName("error")
        INFRASTRUCTURE_ERROR,

        @SerializedName("failure")
        ASSERTION_FAILED
    }
}
