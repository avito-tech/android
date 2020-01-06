package com.avito.http

import com.avito.test.http.MockDispatcher
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class FallbackInterceptorTest {

    private val mockDispatcher = MockDispatcher()
    private val server: MockWebServer = MockWebServer().apply { setDispatcher(mockDispatcher) }

    private val doFallbackOnThisResponseCode = 503

    private val api: FakeApi by lazy {
        createApi(
            baseUrl = server.url("/")
        ) {
            addInterceptor(
                FallbackInterceptor(
                    doFallbackOnTheseCodes = listOf(doFallbackOnThisResponseCode),
                    fallbackRequest = { request ->
                        request.newBuilder()
                            .url(request.url().newBuilder().addPathSegment("fallback").build())
                            .addHeader("X-FALLBACK", "true")
                            .build()
                    })
            )
        }
    }

    @Test
    fun `request success - response is successful`() {
        mockDispatcher.mockResponse(
            requestMatcher = { true },
            response = MockResponse().setResponseCode(200)
        )

        val result = api.request().execute()

        assertThat(result.isSuccessful).isTrue()
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `request failed - fallback is successful`() {
        mockDispatcher.mockResponse(
            requestMatcher = { path == "/" },
            response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
        )

        mockDispatcher.mockResponse(
            requestMatcher = { path == "/fallback" },
            response = MockResponse().setResponseCode(200)
        )

        val fallbackRequest = mockDispatcher.captureRequest { path.contains("fallback") }

        val result = api.request().execute()

        fallbackRequest.checks.singleRequestCaptured().containsHeader("X-FALLBACK", "true")

        assertThat(result.isSuccessful).isTrue()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `request failed - fallback is failed`() {
        mockDispatcher.mockResponse(
            requestMatcher = { path == "/" },
            response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
        )

        mockDispatcher.mockResponse(
            requestMatcher = { path == "/fallback" },
            response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
        )

        val fallbackRequest = mockDispatcher.captureRequest { path.contains("fallback") }

        val result = api.request().execute()

        fallbackRequest.checks.singleRequestCaptured().containsHeader("X-FALLBACK", "true")

        assertThat(result.isSuccessful).isFalse()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }
}
