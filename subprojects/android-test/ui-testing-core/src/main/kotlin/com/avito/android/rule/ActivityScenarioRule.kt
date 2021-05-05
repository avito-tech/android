package com.avito.android.rule

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import java.lang.ref.WeakReference

/**
 * This rule provides functional testing of a single [Activity] similar to deprecated
 * [androidx.test.rule.ActivityTestRule].
 * @param activityClass The Activity class under test
 * @param launchActivity true if the Activity should be launched before each test
 */
open class ActivityScenarioRule<A : Activity>(
    private val activityClass: Class<A>,
    private val launchActivity: Boolean
) : ExternalResource() {

    val activity: A
        get() = activityRef.get() ?: throwActivityIsNotLaunchedException()
    val activityResult: Instrumentation.ActivityResult
        get() = scenario?.result ?: throwActivityIsNotLaunchedException()

    private val activityIsNotLaunchedMessage = "Activity $activityClass is not launched"

    private var scenario: ActivityScenario<A>? = null
    private var activityRef: WeakReference<A> = WeakReference(null)

    override fun before() {
        super.before()
        if (launchActivity) {
            scenario = ActivityScenario.launch(activityClass)
            afterActivityLaunched()
        }
    }

    override fun after() {
        super.after()
        with(checkNotNull(scenario) { activityIsNotLaunchedMessage }) {
            close()
            afterActivityFinished()
        }
        activityRef.clear()
    }

    fun launchActivity(intent: Intent? = null): A {
        scenario = when (intent) {
            null -> ActivityScenario.launch(activityClass)
            else -> ActivityScenario.launch(
                intent.setClass(InstrumentationRegistry.getInstrumentation().targetContext, activityClass)
            )
        }
        afterActivityLaunched()
        return activityRef.get() ?: throwActivityIsNotLaunchedException()
    }

    fun runOnUiThread(runnable: Runnable) {
        with(checkNotNull(scenario) { activityIsNotLaunchedMessage }) {
            onActivity { runnable.run() }
        }
    }

    private fun throwActivityIsNotLaunchedException(): Nothing = error(activityIsNotLaunchedMessage)

    @CallSuper
    protected open fun afterActivityLaunched() {
        checkNotNull(scenario).onActivity { activityRef = WeakReference(it) }
    }

    @CallSuper
    protected open fun afterActivityFinished() {
        // empty
    }
}
