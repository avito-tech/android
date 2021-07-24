package com.avito.android.ui.test.keyboard

import com.avito.android.rule.InHouseScenarioScreenRule
import com.avito.android.test.Device
import com.avito.android.ui.KeyboardActivity
import com.avito.android.ui.test.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

abstract class AbstractKeyboardTest(private val softInputMode: Int) {

    @get:Rule
    val rule = object : InHouseScenarioScreenRule<KeyboardActivity>(KeyboardActivity::class.java) {}

    @Before
    fun before() {
        rule.launchActivity(null)
        setSoftInputMode(softInputMode)
    }

    @Test
    fun openActivity_keyboardIsNotDisplayed() {
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun openedKeyboard_isDisplayed_whenClickedOnTextInput() {
        Screen.keyboardScreen.input.actions.click()
        Device.keyboard.checks.isDisplayed()
    }

    @Test
    fun openedKeyboard_isDisplayed_whenInputMethodUsed() {
        openKeyboard()
        Device.keyboard.checks.isDisplayed()
    }

    @Test
    fun closedKeyboard_isNotDisplayed_whenEspressoUsed() {
        openKeyboard()
        Device.keyboard.close()
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun closedKeyboard_isNotDisplayed_whenInputMethodUsed() {
        openKeyboard()
        hideKeyboard()
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun keyboardChecks_succeed_whenScreenRotated() {
        openKeyboard()
        Device.rotate()
        Device.keyboard.checks.isNotDisplayed() // Keyboard closes after rotation

        openKeyboard()
        Device.keyboard.checks.isDisplayed()

        Device.rotate()
        Device.keyboard.checks.isNotDisplayed()
    }

    @Test
    fun isDisplayed_throwsAssertionError_whenKeyboardIsClosed() {
        assertThrows<AssertionError> { Device.keyboard.checks.isDisplayed() }
    }

    @Test
    fun isNotDisplayed_throwsAssertionError_whenKeyboardIsOpened() {
        openKeyboard()
        assertThrows<AssertionError> { Device.keyboard.checks.isNotDisplayed() }
    }

    private fun setSoftInputMode(mode: Int) {
        rule.scenario.onActivity { activity ->
            activity.window.setSoftInputMode(mode)
        }
    }

    private fun openKeyboard() {
        rule.scenario.onActivity {
            require(it is KeyboardActivity)
            it.openKeyboard()
        }
    }

    private fun hideKeyboard() {
        rule.scenario.onActivity {
            require(it is KeyboardActivity)
            it.hideKeyboard()
        }
    }
}
