package com.avito.utils.gradle

import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable

internal class EnvArgsImpl(project: Project) : EnvArgs, Serializable {

    /**
     * todo make local by default?
     */
    override val build: EnvArgs.Build = when (project.getOptionalStringProperty("avito.build", "teamcity")) {
        "teamcity" -> {
            val teamcityBuildId = project.getMandatoryIntProperty("teamcityBuildId")
            EnvArgs.Build.Teamcity(
                id = teamcityBuildId,
                url = project.getMandatoryStringProperty("teamcityUrl") +
                        "/viewLog.html?buildId=$teamcityBuildId&tab=buildLog",
                number = project.getMandatoryStringProperty("buildNumber"),
                type = "teamcity-${project.getMandatoryStringProperty("teamcityBuildType")}"
            )
        }
        "local" ->
            EnvArgs.Build.Local(EnvArgs.Build.Local.Id.FOR_LOCAL_KUBERNETES_RUN)

        else ->
            throw IllegalStateException("property avito.build must be 'teamcity' or 'local'")
    }
}
