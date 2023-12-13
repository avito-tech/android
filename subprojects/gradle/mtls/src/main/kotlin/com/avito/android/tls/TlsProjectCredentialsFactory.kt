package com.avito.android.tls

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.credentials.TlsCredentialsFactory
import com.avito.android.tls.exception.TlsCredentialsRetrievingInformation
import com.avito.android.tls.exception.TlsNotFoundException
import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration
import com.avito.android.tls.internal.provider.TlsCredentialsProviderFactory

public class TlsProjectCredentialsFactory(
    private val configurations: Set<TlsCredentialsProviderConfiguration>,
) : TlsCredentialsFactory {

    private val tlsCredentialsProviderFactory = TlsCredentialsProviderFactory()

    override fun createCredentials(): TlsCredentials {
       configurations.forEach { configuration ->
           val credentialsProvider = tlsCredentialsProviderFactory.createProvider(configuration)
           val credentials = credentialsProvider.provideCredentials()
           if (credentials != TlsCredentials.Undefined) {
               return credentials
           }
       }

        throwExceptionWithInstructions()
    }

    private fun throwExceptionWithInstructions(): Nothing {
        val retrievingInformation = configurations
            .mapNotNull { it.helperText.orNull }
            .map(::TlsCredentialsRetrievingInformation)

        throw TlsNotFoundException(retrievingInformation)
    }
}
