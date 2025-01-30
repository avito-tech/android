package com.avito.teamcity

import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.BuildLocator
import org.jetbrains.teamcity.rest.Project
import org.jetbrains.teamcity.rest.ProjectId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory

internal class TeamcityApiImpl private constructor(private val teamCityInstance: TeamCityInstance) : TeamcityApi {

    constructor(credentials: TeamcityCredentials) : this(credentials.url, credentials.user, credentials.password)

    private constructor(url: String, user: String, password: String) : this(
        TeamCityInstanceFactory.httpAuth(
            url,
            user,
            password
        ).withLogResponses()
    )

    override fun getProjectById(id: ProjectId): Project {
        return teamCityInstance.project(id)
    }

    override fun getProjectByBuildConfiguration(id: BuildConfigurationId): Project {
        val projectId = teamCityInstance.buildConfiguration(id).projectId
        return teamCityInstance.project(projectId)
    }

    override fun getBuilds(buildType: String, builder: BuildLocator.() -> Unit): Sequence<Build> {
        val builds = teamCityInstance.builds()
            .fromConfiguration(BuildConfigurationId(buildType))
        builder(builds)
        return builds.all()
    }

    override fun getBuild(buildId: String): Build = teamCityInstance.build(BuildId(buildId))

    @Deprecated("Use getBuild instead")
    override fun getBuildNumber(buildId: String): String? = teamCityInstance.build(BuildId(buildId)).buildNumber

    override fun triggerBuild(
        buildType: String,
        comment: String,
        branchName: String,
        parameters: Map<String, String>
    ): Build {
        return teamCityInstance.buildConfiguration(BuildConfigurationId(buildType))
            .runBuild(parameters = parameters, comment = comment, logicalBranchName = branchName)
    }
}
