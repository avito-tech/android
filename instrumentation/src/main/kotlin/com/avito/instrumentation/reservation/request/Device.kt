package com.avito.instrumentation.reservation.request

import java.io.Serializable

sealed class Device : Serializable {

    abstract val name: String
    abstract val api: Int

    sealed class Emulator(
        override val name: String,
        override val api: Int,
        val image: String,
        val gpu: Boolean = false,
        val cpuCoresLimit: String,
        val cpuCoresRequest: String = cpuCoresLimit
    ) : Device() {

        private object Image {
            const val emulator_22 = "android/emulator-22:116d6ed6c6"
            const val emulator_23 = "android/emulator-23:33578ce220"
            const val emulator_24 = "android/emulator-24:46b6a74473"
            const val emulator_27 = "android/emulator-27:1c36e79d44"
            const val emulator_28 = "android/emulator-28:8a298b5bf6"
        }

        object Emulator22 : Emulator(
            api = 22,
            image = Image.emulator_22,
            name = "phone_22",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator23 : Emulator(
            api = 23,
            image = Image.emulator_23,
            name = "phone_23",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator24 : Emulator(
            api = 24,
            image = Image.emulator_24,
            name = "phone_24",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator24Cores2 : Emulator(
            api = 24,
            image = Image.emulator_24,
            name = "phone_24_cores_2",
            gpu = false,
            cpuCoresLimit = "2",
            cpuCoresRequest = "1.5"
        )

        object Emulator27 : Emulator(
            api = 27,
            image = Image.emulator_27,
            name = "phone_27",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator28 : Emulator(
            api = 28,
            image = Image.emulator_28,
            name = "phone_28",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

    }

    sealed class Phone(
        override val name: String,
        override val api: Int,
        val proxyImage: String
    ) : Device() {

        // TODO: api aware real devices reservations
        object RealPhone : Phone(
            name = "real_phone",
            api = 22,
            proxyImage = "android/device:8299de168b"
        )
    }
}
