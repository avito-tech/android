package com.avito.math

import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat

@Suppress("MagicNumber")
public fun Int.percentOf(sum: Int): Percent = BigDecimalTwoDigitsPrecisionPercent.create(toFloat() / sum * 100)

@Suppress("MagicNumber")
public fun Long.percentOf(sum: Long): Percent = BigDecimalTwoDigitsPrecisionPercent.create(toFloat() / sum * 100)

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
public fun Double.fromZeroToOnePercent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return BigDecimalTwoDigitsPrecisionPercent.create(this * 100)
}

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
public fun Float.fromZeroToOnePercent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return BigDecimalTwoDigitsPrecisionPercent.create(this * 100)
}

public fun Int.fromZeroToHundredPercent(): Percent {
    require(this in 0..100) { "Trying to convert $this to percents; should be in [0..100] range" }
    return BigDecimalTwoDigitsPrecisionPercent.create(this)
}

public interface Percent {

    public fun roundToTwoDigitsPrecision(): String

    public fun roundToInt(): Int

    public fun roundToLong(): Long

    public companion object {
        public val ZERO: Percent = BigDecimalTwoDigitsPrecisionPercent.create(0)
    }
}

internal data class BigDecimalTwoDigitsPrecisionPercent private constructor(val value: BigDecimal) : Percent {

    private val formatted: String by lazy { DecimalFormat("0.##'%'").format(value) }

    override fun roundToTwoDigitsPrecision(): String = formatted

    override fun roundToInt(): Int = value.toInt()

    override fun roundToLong(): Long = value.toLong()

    override fun toString(): String = formatted

    companion object {
        fun create(number: Number): Percent {
            val mathContext = MathContext(4)
            return when (number) {
                is Double -> BigDecimalTwoDigitsPrecisionPercent(BigDecimal(number, mathContext))
                is Float -> BigDecimalTwoDigitsPrecisionPercent(BigDecimal(number.toDouble(), mathContext))
                is Long -> BigDecimalTwoDigitsPrecisionPercent(BigDecimal(number, mathContext))
                else -> BigDecimalTwoDigitsPrecisionPercent(BigDecimal(number.toLong(), mathContext))
            }
        }
    }
}
