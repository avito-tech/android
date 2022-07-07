package com.avito.emcee.worker.model

import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.Bucket
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.PayloadContainer
import com.avito.emcee.queue.RemoteApk
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestName
import com.avito.emcee.worker.GetBucketResponse

internal fun GetBucketResponse.Companion.dequeued(): GetBucketResponse.Dequeued = GetBucketResponse.Dequeued(
    caseId = "bucketDequeued",
    bucket = Bucket(
        bucketId = "1F10555C-0D48-436F-B0A6-4D0ABF813493",
        payloadContainer = PayloadContainer(
            payload = Payload(
                testEntries = listOf(
                    TestEntry(
                        caseId = null,
                        tags = emptyList(),
                        name = TestName(
                            className = "SomeClassNameWithTests",
                            methodName = "testMethod"
                        )
                    ),
                    TestEntry(
                        caseId = null,
                        tags = emptyList(),
                        name = TestName(
                            className = "AnotherClass",
                            methodName = "test"
                        )
                    )
                ),
                testConfiguration = TestConfiguration(
                    buildArtifacts = BuildArtifacts(
                        app = RemoteApk(
                            location = ApkLocation(url = "https://example.com/artifactory/repo/path/app.apk"),
                            packageName = "com.avito.android"
                        ),
                        testApp = RemoteApk(
                            location = ApkLocation("https://example.com/artifactory/repo/path/test.apk"),
                            packageName = "com.avito.android.test"
                        ),
                        runnerClass = "com.avito.android.InstrumentationRunner"
                    ),
                    deviceType = "Nexus 5",
                    sdkVersion = 30,
                    testExecutionBehavior = TestExecutionBehavior(
                        environment = mapOf("SOME" to "env values"),
                        retries = 5
                    ),
                    testMaximumDurationSec = 30
                )
            )
        )
    )
)

internal fun GetBucketResponse.Companion.noBucket() =
    GetBucketResponse.NoBucket(caseId = "checkAgainLater", checkAfter = 30)
