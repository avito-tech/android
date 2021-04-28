package com.avito.android.test.report.model

import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.TestCaseBehavior
import com.avito.report.model.TestCasePriority
import java.io.Serializable

data class TestMetadata(
    val caseId: Int?,
    val description: String?,
    val className: String,
    val methodName: String?,
    val dataSetNumber: Int?,
    val kind: Kind,
    val priority: TestCasePriority?,
    val behavior: TestCaseBehavior?,
    val externalId: String?,
    val featureIds: List<Int>,
    val tagIds: List<Int>,
    val flakiness: Flakiness
) : Serializable {

    val testName = "$className.$methodName"

    companion object
}
