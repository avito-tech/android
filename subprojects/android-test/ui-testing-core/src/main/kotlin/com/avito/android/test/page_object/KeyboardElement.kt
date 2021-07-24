package com.avito.android.test.page_object

import android.app.Activity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.avito.android.util.waitForAssertion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import java.io.IOException

class KeyboardElement : PageObject() {

    val actions = ActionLibrary()

    val checks = CheckLibrary()

    fun close() {
        Espresso.closeSoftKeyboard()
    }

    class ActionLibrary {

        fun pressImeAction(element: PageObjectElement) {
            element.interactionContext.perform(ViewActions.pressImeActionButton())
        }
    }

    class CheckLibrary {

        /**
         * For backward compatibility
         */
        fun isDisplayed(@Suppress("UNUSED_PARAMETER") activity: Activity?) = isDisplayed()

        /**
         * For backward compatibility
         */
        fun isNotDisplayed(@Suppress("UNUSED_PARAMETER") activity: Activity?) = isNotDisplayed()

        fun isDisplayed() {
            waitForAssertion {
                checkDisplayed(true)
            }
        }

        fun isNotDisplayed() {
            waitForAssertion {
                checkDisplayed(false)
            }
        }

        private fun checkDisplayed(expected: Boolean) {
            val output: String
            try {
                output = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand(KEYBOARD_STATE_CHECK_CMD)
                    .split("\n")
                    .first { it.contains(KEYBOARD_STATE_FLAG) }
            } catch (e: IOException) {
                throw RuntimeException("An error occurred while executing '$KEYBOARD_STATE_CHECK_CMD'", e)
            }
            val actual = output.contains(KEYBOARD_STATE_FLAG_OPENED)
            assertThat(generateErrorMessage(expected, actual, output), actual, `is`(expected))
        }

        private fun generateErrorMessage(
            expected: Boolean,
            actual: Boolean,
            cmdOutput: String
        ): String {

            val message: (isDisplayed: Boolean) -> String = { isDisplayed ->
                when (isDisplayed) {
                    true -> "is displayed"
                    false -> "is not displayed"
                }
            }

            return buildString {
                append("Keyboard ${message(actual)} on the screen. ")
                append("Expected: ${message(expected)}. ")
                append("Cmd output is:\n\"$cmdOutput\"")
            }
        }

        companion object {
            private const val KEYBOARD_STATE_CHECK_CMD = "dumpsys input_method | grep mInputShown"
            private const val KEYBOARD_STATE_FLAG = "mInputShown"
            private const val KEYBOARD_STATE_FLAG_OPENED = "mInputShown=true"
        }
    }
}
