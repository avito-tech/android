package com.avito.report

import com.avito.test.http.MockDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class MockReportsExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private var state: State? = null

    override fun beforeEach(context: ExtensionContext) {
        val mockDispatcher = MockDispatcher()
        val mockWebServer = MockWebServer().apply { dispatcher = mockDispatcher }
        state = State(
            mockWebServer = mockWebServer,
            mockReportApi = MockReportApi(
                realApi = ReportsApi.create(
                    host = mockWebServer.url("/").toString(),
                    fallbackUrl = "",
                    logger = { message, error ->
                        println(message)
                        error?.printStackTrace()
                    }
                ),
                mockDispatcher = mockDispatcher
            )
        )
    }

    override fun afterEach(context: ExtensionContext) {
        state?.release()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == MockReportApi::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return state?.mockReportApi
    }

    companion object {
        @Suppress("unused")
        private val NAMESPACE = ExtensionContext.Namespace.create(MockReportsExtension::class.java)
    }

    private class State(
        val mockWebServer: MockWebServer,
        val mockReportApi: MockReportApi
    ) {
        fun release() {
            mockWebServer.shutdown()
        }
    }
}
