package com.avito.s3

import java.net.URL

/**
 * @param endpointUrl s3 compatible endpoint
 */
public class S3ClientConfig(
    public val region: String = "us-east-1",
    public val endpointUrl: URL,
    public val accessKeyId: String,
    public val secretAccessKey: String,
)
