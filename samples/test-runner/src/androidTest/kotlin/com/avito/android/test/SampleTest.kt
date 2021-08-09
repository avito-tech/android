package com.avito.android.test

import com.google.common.truth.Truth
import org.junit.Test

class SampleTest {

    @Test
    fun test() {
        Truth.assertThat(2L + 2).isEqualTo(4)
    }

    @Test
    fun twoSecTest() {
        Thread.sleep(2000)
        Truth.assertThat(2L + 2).isEqualTo(4)
    }

    @Test
    fun tenSecTest() {
        Thread.sleep(10000)
        Truth.assertThat(2L + 2).isEqualTo(4)
    }
}
