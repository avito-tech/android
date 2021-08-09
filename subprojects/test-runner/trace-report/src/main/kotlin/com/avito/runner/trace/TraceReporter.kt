package com.avito.runner.trace

import com.avito.runner.listener.DeviceListener

public interface TraceReporter : DeviceListener {

    public fun report()
}
