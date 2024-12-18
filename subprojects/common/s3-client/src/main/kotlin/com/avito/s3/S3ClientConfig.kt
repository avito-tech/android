package com.avito.s3

import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @param endpointUrl s3 compatible endpoint
 */
public class S3ClientConfig(
    public val region: String = "us-east-1",
    public val endpointUrl: URL,
    public val accessKeyId: String,
    public val secretAccessKey: String,
    public val httpClientConfig: HttpClientConfig,
) {
    public class HttpClientConfig(
        public val maxConcurrency: UInt = 10u,
        public val connectionIdleTimeout: Duration = 120.seconds,
    )
}
