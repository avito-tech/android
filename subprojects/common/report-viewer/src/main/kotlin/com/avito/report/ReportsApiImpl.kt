package com.avito.report

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.model.ConclusionStatus
import com.avito.report.internal.model.CreateResponse
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.internal.model.RpcResult
import com.avito.report.model.CreateResult
import com.avito.report.model.CreateResult.AlreadyCreated
import com.avito.report.model.CreateResult.Created
import com.avito.report.model.CreateResult.Failed
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.google.gson.JsonElement

internal class ReportsApiImpl(
    private val loggerFactory: LoggerFactory,
    private val requestProvider: JsonRpcRequestProvider
) : ReportsApi,
    ReportsAddApi by ReportsAddApiImpl(requestProvider),
    ReportsFetchApi by ReportsFetchApiImpl(requestProvider, loggerFactory) {

    override fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        testHost: String,
        gitBranch: String,
        gitCommit: String,
        tmsBranch: String
    ): CreateResult {
        return try {
            val result = requestProvider.jsonRpcRequest<RpcResult<CreateResponse>>(
                RfcRpcRequest(
                    method = "Run.Create",
                    params = mapOf(
                        "plan_slug" to reportCoordinates.planSlug,
                        "job_slug" to reportCoordinates.jobSlug,
                        "run_id" to reportCoordinates.runId,
                        "report_data" to mapOf(
                            "build" to buildId,
                            "testHost" to testHost,
                            "testsBranch" to tmsBranch,
                            "appBranch" to gitBranch,

                            /**
                             * to filter report history
                             */
                            "tags" to listOf(
                                "buildBranch:$gitBranch",
                                "buildCommit:$gitCommit"
                            )
                        )
                    )
                )
            )
            Created(result.result.id)
        } catch (e: Throwable) {
            val isDuplicateKeyError = e.message?.contains("duplicate key error collection") ?: false
            if (isDuplicateKeyError) {
                AlreadyCreated
            } else {
                Failed(e)
            }
        }
    }

    override fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit> {
        return when (val getReportResult = getReport(reportCoordinates)) {
            is GetReportResult.Found -> Result.tryCatch {
                requestProvider.jsonRpcRequest<Unit>(
                    RfcRpcRequest(
                        method = "Run.SetFinished",
                        params = mapOf(
                            "id" to getReportResult.report.id
                        )
                    )
                )
            }
            GetReportResult.NotFound -> Result.Failure(Exception("Report not found $reportCoordinates"))
            is GetReportResult.Error -> Result.Failure(getReportResult.exception)
        }
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.OK,
            comment
        )
    }

    override fun markAsFailed(testRunId: String, author: String, comment: String): Result<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.FAIL,
            comment
        )
    }

    override fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Result<Unit> {
        return Result.tryCatch {
            requestProvider.jsonRpcRequest<Unit>(
                RfcRpcRequest(
                    method = "Run.PushPreparedData",
                    params = mapOf(
                        "id" to reportId,
                        "analyzer_key" to analyzerKey,
                        "prepared_data" to preparedData
                    )
                )
            )
        }
    }

    /**
     * RunTest.AddConclusion
     * @param status 'ok','fail','irrelevant' ('irrelevant' допустим только для тестов в статусе 32 - testcase)
     */
    private fun addConclusion(id: String, author: String, status: ConclusionStatus, comment: String): Result<Unit> {
        return Result.tryCatch {
            requestProvider.jsonRpcRequest<Unit>(
                RfcRpcRequest(
                    method = "RunTest.AddConclusion",
                    params = mapOf(
                        "id" to id,
                        "author" to author,
                        "status" to status,
                        "comment" to comment
                    )
                )
            )
        }
    }
}
