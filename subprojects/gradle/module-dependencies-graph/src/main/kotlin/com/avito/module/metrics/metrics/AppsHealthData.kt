package com.avito.module.metrics.metrics

import com.avito.math.Percent
import com.avito.math.percentOf
import org.gradle.util.Path

internal data class AppsHealthData(
    val absolute: Map<Path, AbsoluteMetrics>,
    val relative: Matrix<Path, RelativeMetrics>
)

internal class AbsoluteMetrics(
    val allDependencies: Int
)

internal class RelativeMetrics(
    val baselineDependencies: Set<Path>,
    val comparedDependencies: Set<Path>,
) {

    val commonDependencies: Set<Path>
        get() = baselineDependencies.intersect(comparedDependencies)

    val uniqueDependencies: Set<Path>
        get() = baselineDependencies.minus(commonDependencies)

    /**
     * Proportion of compared dependencies included to baseline dependencies
     *
     * Example:
     * application X has modules A, B
     * application Y has modules B
     *
     * X -> Y: 50% (includes 1 out of 2 dependencies)
     * Y -> X: 100% (includes 1 out of 1)
     */
    val commonDependenciesRatio: Percent
        get() = if (baselineDependencies.isEmpty()) {
            Percent.ZERO
        } else {
            commonDependencies.size.percentOf(baselineDependencies.size)
        }
}
