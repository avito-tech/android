package com.avito.s3

import com.avito.android.Result
import java.io.File
import java.net.URL

public interface S3Client {
    /**
     * @param key - relative [objekt] path in [s3Bucket]
     */
    public suspend fun putObject(
        key: String,
        objekt: File,
    ): Result<URL>

    public companion object {
        public fun create(config: S3ClientConfig): S3Client {
            return S3ClientImpl(config)
        }
    }
}
