package com.avito.math

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PercentKtTest {

    @Test
    fun `percent of int - round is correct - no decimal`() {
        assertThat(4.percentOf(8).roundToInt()).isEqualTo(50)
    }

    @Test
    fun `percent of int - round is correct - decimal`() {
        assertThat(5.percentOf(8).roundToInt()).isEqualTo(62)
    }

    @Test
    fun `percent of int - two decimal string is correct - no decimal`() {
        assertThat(4.percentOf(8).roundToTwoDigitsPrecision()).isEqualTo("50%")
    }

    @Test
    fun `percent of int - two decimal string is correct - decimal`() {
        assertThat(5.percentOf(8).roundToTwoDigitsPrecision()).isEqualTo("62.5%")
    }

    @Test
    fun `double percent - two decimal string is correct - decimal`() {
        assertThat(0.2345.fromZeroToOnePercent().roundToTwoDigitsPrecision()).isEqualTo("23.45%")
    }

    @Test
    fun `float percent - two decimal string is correct - decimal`() {
        assertThat(0.2345F.fromZeroToOnePercent().roundToTwoDigitsPrecision()).isEqualTo("23.45%")
    }

    @Test
    fun `percent - throws exception - not in range`() {
        assertThrows<IllegalArgumentException> {
            23.45.fromZeroToOnePercent()
        }
    }

    @Test
    fun `NaN - throws exception`() {
        assertThrows<IllegalArgumentException> {
            Float.NaN.fromZeroToOnePercent()
        }
    }

    @Test
    fun `Infinity - throws exception`() {
        assertThrows<IllegalArgumentException> {
            Float.NEGATIVE_INFINITY.fromZeroToOnePercent()
        }
        assertThrows<IllegalArgumentException> {
            Float.POSITIVE_INFINITY.fromZeroToOnePercent()
        }
    }
}
