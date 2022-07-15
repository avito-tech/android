package com.avito.runner.scheduler.runner.scheduler

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.retry.SchedulingBasedRetryManager
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.test.model.TestCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

internal class TestExecutionScheduler(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val results: SendChannel<TestRunResult>,
    private val intentions: SendChannel<Intention>,
    private val intentionResults: ReceiveChannel<IntentionResult>,
) {

    private val states: MutableMap<TestCase, TestExecutionState> = mutableMapOf()

    suspend fun start(requests: List<TestRunRequest>) {
        with(CoroutineScope(coroutineContext + dispatcher)) {
            launch(CoroutineName("test-state-verdict")) {
                for (result in intentionResults) {
                    val state = requireNotNull(states[result.intention.action.test]) {
                        "Can't find state for $result"
                    }
                    when (val verdict = state.verdict(result.actionResult.testCaseRun)) {
                        is TestExecutionState.Verdict.SendResult ->
                            results.send(
                                TestRunResult(
                                    request = state.request,
                                    result = verdict.results
                                )
                            )

                        is TestExecutionState.Verdict.Run ->
                            verdict.intentions.forEach { intention ->
                                intentions.send(intention)
                            }
                        TestExecutionState.Verdict.DoNothing -> {
                            // do nothing
                        }
                    }
                }
            }
            launch(CoroutineName("test-state-verdict")) {
                for (request in requests) {
                    val testState =
                        TestExecutionStateImplementation(
                            request = request,
                            retryManager = SchedulingBasedRetryManager(
                                scheduling = request.scheduling
                            )
                        )
                    states[request.testCase] = testState

                    when (val verdict = testState.verdict(incomingTestCaseRun = null)) {
                        is TestExecutionState.Verdict.Run ->
                            verdict.intentions.forEach { intention ->
                                intentions.send(intention)
                            }

                        is TestExecutionState.Verdict.SendResult ->
                            throw IllegalStateException("Trying to send empty result")
                        TestExecutionState.Verdict.DoNothing -> {
                            // do nothing
                        }
                    }
                }
            }
        }
    }
}
