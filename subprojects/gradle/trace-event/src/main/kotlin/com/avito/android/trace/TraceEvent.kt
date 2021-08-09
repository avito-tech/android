package com.avito.android.trace

import com.google.gson.annotations.SerializedName

public interface TraceEvent {

    /**
     * The name of the event, as displayed in Trace Viewer
     */
    public val eventName: String

    /**
     * The event categories.
     * This is a comma separated list of categories for the event.
     * The categories can be used to hide events in the Trace Viewer UI.
     */
    public val categories: String?

    /**
     * The event type.
     * This is a single character which changes depending on the type of event being output
     */
    public val phase: Char
    public val processId: String
    public val threadId: String?

    /**
     * The tracing clock timestamp of the event.
     * Somehow `displayTimeUnit` doesn't affect this time unit.
     */
    public val timestampMicroseconds: Long

    /**
     * A fixed color name:
     * https://github.com/catapult-project/catapult/blob/master/tracing/tracing/base/color_scheme.html
     */
    public val color: String?

    /**
     * Any arguments provided for the event.
     * Some of the event types have required argument fields, otherwise, you can put any information you wish in here.
     * The arguments are displayed in Trace Viewer when you view an event in the analysis section.
     */
    public val args: Map<String, Any>?

    public companion object {
        public const val COLOR_GOOD: String = "good"
        public const val COLOR_BAD: String = "bad"
        public const val COLOR_TERRIBLE: String = "terrible"

        public const val COLOR_BLACK: String = "black"
        public const val COLOR_GREY: String = "grey"
        public const val COLOR_WHITE: String = "white"
        public const val COLOR_YELLOW: String = "yellow"
        public const val COLOR_BLUE: String = "rail_response"
        public const val COLOR_LIGHT_GREEN: String = "rail_load"
        public const val COLOR_LIGHT_RED: String = "rail_animation"
        public const val COLOR_ORANGE: String = "rail_idle"
    }
}

public data class DurationEvent(
    @SerializedName("ph") override val phase: Char,
    @SerializedName("ts") override val timestampMicroseconds: Long,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken
    // and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
) : TraceEvent {

    public companion object {
        public const val PHASE_BEGIN: Char = 'B'
        public const val PHASE_END: Char = 'E'
    }
}

/**
 * Each complete event logically combines a pair of duration events.
 * Unlike duration events, the timestamps of complete events can be in any order.
 */
public data class CompleteEvent(
    @SerializedName("ph") override val phase: Char = PHASE,
    /**
     * The time of the start of the complete event
     */
    @SerializedName("ts") override val timestampMicroseconds: Long,
    /**
     * The tracing clock duration of complete events in microseconds
     */
    @SerializedName("dur") val durationMicroseconds: Long,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken
    // and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
) : TraceEvent {

    internal companion object {
        const val PHASE = 'X'
    }
}

/**
 * Something that happens but has no duration associated with it
 */
public data class InstantEvent(
    @SerializedName("ph") override val phase: Char = PHASE,
    /**
     * The time of the start of the complete event
     */
    @SerializedName("ts") override val timestampMicroseconds: Long,
    /**
     * The scope of the event designates how tall to draw the instant event in Trace Viewer.
     * - A thread scoped event will draw the height of a single thread.
     * - A process scoped event will draw through all threads of a given process.
     * - A global scoped event will draw a time from the top to the bottom of the timeline.
     */
    @SerializedName("s") val scope: String,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken
    // and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
) : TraceEvent {

    public companion object {
        public const val PHASE: Char = 'i'
        public const val SCOPE_THREAD: String = "t"
        public const val SCOPE_PROCESS: String = "p"
        public const val SCOPE_GLOBAL: String = "g"
    }
}
