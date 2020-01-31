package com.avito.bitbucket

/**
 * The data field on the report is an array with at most 6 data fields (JSON maps) containing information that is to be displayed on the report
 *
 * @param type The type of data contained in the value field. If not provided, then the value will be detected as a boolean, number or string.
 *             One of: BOOLEAN, DATE, DURATION, LINK, NUMBER, PERCENTAGE, TEXT
 * @param value A value based on the type provided. Either a raw value (string, number or boolean) or a map.
 */
sealed class InsightData(val type: String) {

    /**
     * A string describing what this data field represents
     */
    abstract val title: String

    data class Text(override val title: String, val value: String) : InsightData(type = "TEXT")

    data class Number(override val title: String, val value: kotlin.Number) : InsightData(type = "NUMBER")

    /**
     * @param value time in millis
     */
    data class Duration(override val title: String, val value: Long) : InsightData(type = "DURATION")
}
