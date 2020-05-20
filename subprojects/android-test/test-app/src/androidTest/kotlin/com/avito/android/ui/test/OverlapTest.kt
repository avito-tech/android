package com.avito.android.ui.test

import com.avito.android.ui.OverlapActivity
import org.junit.Rule
import org.junit.Test

class OverlapTest {

    @get:Rule
    val rule = screenRule<OverlapActivity>()

    @Test
    fun isOverlapped_button_under_snackbar() {
        rule.launchActivity(null)

        Screen.overlapScreen.snackButton.click()

        Screen.overlapScreen.snackButton.checks.isOverlapped()
    }

    @Test
    fun isNotOverlapped_button_without_snackbar() {
        rule.launchActivity(null)

        Screen.overlapScreen.snackButton.checks.isNotOverlapped()
    }

    @Test
    fun isOverlapped_different_parents() {
        rule.launchActivity(null)

        Screen.overlapScreen.overlappedText.checks.isOverlapped()
    }

    @Test
    fun isOverlapped_same_parent() {
        rule.launchActivity(null)

        Screen.overlapScreen.redGroup.checks.isOverlapped()
    }

    @Test
    fun isNotOverlapped_intersect_but_not_overlap() {
        rule.launchActivity(null)

        Screen.overlapScreen.greenGroup.checks.isNotOverlapped()
    }

}