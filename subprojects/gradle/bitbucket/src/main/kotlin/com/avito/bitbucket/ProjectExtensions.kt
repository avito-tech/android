package com.avito.bitbucket

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalIntProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

val Project.atlassianCredentials: Provider<AtlassianCredentials> by ProjectProperty.lazy { project ->
    if (project.buildEnvironment is BuildEnvironment.CI) {
        Providers.of(atlassianCredentials(project))
    } else {
        Providers.notDefined<AtlassianCredentials>()
    }
}

val Project.pullRequestId: Provider<Int> by ProjectProperty.lazy { project ->
    if (project.buildEnvironment is BuildEnvironment.CI) {
        val pullRequestId = project.getOptionalIntProperty("pullRequestId")
        if (pullRequestId != null) {
            Providers.of(pullRequestId)
        } else {
            Providers.notDefined<Int>()
        }
    } else {
        Providers.notDefined<Int>()
    }
}

val Project.bitbucketConfig: Provider<BitbucketConfig> by ProjectProperty.lazy { project ->
    Providers.of(
        BitbucketConfig(
            baseUrl = project.getMandatoryStringProperty("avito.bitbucket.url"),
            projectKey = project.getMandatoryStringProperty("avito.bitbucket.projectKey"),
            repositorySlug = project.getMandatoryStringProperty("avito.bitbucket.repositorySlug"),
            credentials = atlassianCredentials(project)
        )
    )
}

//todo добавить префиксы avito.
private fun atlassianCredentials(project: Project): AtlassianCredentials = AtlassianCredentials(
    project.getMandatoryStringProperty("atlassianUser"),
    project.getMandatoryStringProperty("atlassianPassword")
)
