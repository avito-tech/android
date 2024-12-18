package com.avito.s3

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import com.avito.android.Result
import java.io.File
import java.net.URL
import aws.sdk.kotlin.services.s3.S3Client as AWS_S3Client

internal class S3ClientImpl(
    private val config: S3ClientConfig,
) : S3Client {

    private val awsS3client = AWS_S3Client {
        region = config.region
        endpointUrl = Url.parse(config.endpointUrl.toString())
        credentialsProvider = StaticCredentialsProvider(
            credentials = Credentials(
                accessKeyId = config.accessKeyId,
                secretAccessKey = config.secretAccessKey
            )
        )
        httpClient {
            maxConcurrency = config.httpClientConfig.maxConcurrency
            connectionIdleTimeout = config.httpClientConfig.connectionIdleTimeout
        }
    }

    override suspend fun putObject(
        key: String,
        objekt: File
    ): Result<URL> {
        return Result.tryCatch {
            awsS3client.putObject(
                PutObjectRequest {
                    this.key = key
                    this.metadata = emptyMap()
                    this.body = objekt.asByteStream()
                }
            )
            URL("${config.endpointUrl}/$key")
        }
    }
}
