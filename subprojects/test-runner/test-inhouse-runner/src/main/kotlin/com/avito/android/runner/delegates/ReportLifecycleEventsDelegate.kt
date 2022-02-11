package com.avito.android.runner.delegates

import android.os.Bundle
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import com.avito.android.runner.InstrumentationTestRunnerDelegate
import com.avito.android.test.report.Report
import com.avito.android.test.report.lifecycle.ReportActivityLifecycleListener
import com.avito.logger.LoggerFactory

class ReportLifecycleEventsDelegate(
    factory: LoggerFactory,
    report: Report
) : InstrumentationTestRunnerDelegate() {
    /**
     * ActivityLifecycleMonitorRegistry wraps callbacks by WeakReference.
     * So we holding a reference to avoid the garbage collection
     */
    private val listener =
        com.avito.android.test.report.lifecycle.ReportActivityLifecycleListener(factory, report)

    override fun afterOnCreate(arguments: Bundle) {
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .addLifecycleCallback(listener)
    }

    override fun afterFinish(resultCode: Int, results: Bundle?) {
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .removeLifecycleCallback(listener)
    }
}
