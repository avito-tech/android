package com.avito.utils.logging

import com.avito.android.elastic.ElasticConfig
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project
import java.net.URL

internal object ElasticConfigFactory {

    fun config(project: Project): ElasticConfig {
        val isElasticEnabled = project.properties["avito.elastic.enabled"].toString() == "true"

        return if (isElasticEnabled) {
            val endpoint: String? = project.properties["avito.elastic.endpoint"]?.toString()
            require(!endpoint.isNullOrBlank()) { "avito.elastic.endpoints has not been provided" }

            val indexPattern: String? = project.properties["avito.elastic.indexpattern"]?.toString()
            require(!indexPattern.isNullOrBlank()) { "avito.elastic.indexpattern has not been provided" }

            val buildId = project.envArgs.build.id.toString()

            ElasticConfig.Enabled(
                endpoint = URL(endpoint),
                indexPattern = indexPattern,
                buildId = buildId
            )
        } else {
            ElasticConfig.Disabled
        }
    }
}
