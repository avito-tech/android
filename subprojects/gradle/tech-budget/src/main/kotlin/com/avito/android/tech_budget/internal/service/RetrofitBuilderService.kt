package com.avito.android.tech_budget.internal.service

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.techBudgetExtension
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
import com.avito.logger.LoggerFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal abstract class RetrofitBuilderService : BuildService<RetrofitBuilderService.Params> {

    private val predefinedOkHttpBuilder: OkHttpClient.Builder by lazy {
        prepareDefaultHttpClient()
    }

    fun build(
        loggerFactory: LoggerFactory,
        ownerAdapterFactory: OwnerAdapterFactory = OwnerAdapterFactory(),
    ): Retrofit {
        val moshi = MoshiProvider(ownerAdapterFactory).provide()
        val moshiConverterFactory = MoshiConverterFactory.create(moshi).failOnUnknown()

        val logger = loggerFactory.create("OkHttp")
        val loggingInterceptor = HttpLoggingInterceptor(logger::info)
            .setLevel(HttpLoggingInterceptor.Level.BASIC)

        val okHttpClient = predefinedOkHttpBuilder
            .readTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(okHttpClient)
            .baseUrl(parameters.baseUrl.get())
            .build()
    }

    private fun prepareDefaultHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            if (parameters.useTls.get()) {
                val tlsManager = TlsManager(parameters.tlsCredentialsService.get().createCredentials())
                val handshakeCertificates = tlsManager.handshakeCertificates()
                sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            }
        }
    }

    companion object {
        internal fun provideRetrofitService(
            project: Project,
        ): Provider<RetrofitBuilderService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<RetrofitBuilderService> {
            return project.gradle.sharedServices.registerIfAbsent(
                RetrofitBuilderService::class.java.name,
                RetrofitBuilderService::class.java,
            ) {
                val techBudgetExtension = project.techBudgetExtension
                it.parameters { params ->
                    params.baseUrl.set(techBudgetExtension.dumpInfo.baseUploadUrl)
                    params.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
                    params.useTls.set(techBudgetExtension.dumpInfo.useTls)
                }
            }
        }
    }

    internal interface Params : BuildServiceParameters {
        val baseUrl: Property<String>
        val tlsCredentialsService: Property<TlsCredentialsService>
        val useTls: Property<Boolean>
    }
}

internal fun Task.usesRetrofitBuilderService(serviceProperty: Property<RetrofitBuilderService>) {
    val service = RetrofitBuilderService.provideRetrofitService(project)
    serviceProperty.set(service)
    usesService(service)
}
