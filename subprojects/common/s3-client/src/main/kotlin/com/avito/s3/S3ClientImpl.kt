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
    private val config: S3ClientConfig
) : S3Client {

    override suspend fun putObject(
        s3Bucket: String,
        key: String,
        objekt: File
    ): Result<URL> {
        return Result.tryCatch {
            AWS_S3Client {
                region = config.region
                endpointUrl = Url.parse(config.endpointUrl.toString())
                credentialsProvider = StaticCredentialsProvider(
                    credentials = Credentials(
                        accessKeyId = config.accessKeyId,
                        secretAccessKey = config.secretAccessKey
                    )
                )
            }.use { s3 ->
                s3.putObject(
                    PutObjectRequest {
                        this.bucket = s3Bucket
                        this.key = key
                        this.metadata = emptyMap()
                        this.body = objekt.asByteStream()
                    }
                )
                URL("${config.endpointUrl}/$s3Bucket/$key")
            }
        }
    }
}
