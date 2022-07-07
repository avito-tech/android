package com.avito.emcee.worker

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

public interface WorkerQueueApi {

    @POST("registerWorker")
    public suspend fun registerWorker(@Body body: RegisterWorkerBody): RegisterWorkerResponse

    @POST("getBucket")
    public suspend fun getBucket(@Body body: GetBucketBody): GetBucketResponse

    @POST("bucketResult")
    public suspend fun sendBucketResult(@Body body: SendBucketResultBody)

    public companion object {

        public fun Retrofit.Builder.createWorkerQueueApi(client: OkHttpClient, baseUrl: String): WorkerQueueApi {
            return addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().build()
                ).failOnUnknown()
            )
                .client(client)
                .baseUrl(baseUrl)
                .build()
                .create()
        }
    }
}
