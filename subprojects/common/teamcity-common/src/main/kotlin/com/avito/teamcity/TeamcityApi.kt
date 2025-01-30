package com.avito.teamcity

import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildLocator
import org.jetbrains.teamcity.rest.Project
import org.jetbrains.teamcity.rest.ProjectId

public interface TeamcityApi {

    public sealed class BranchSpec {
        public object DefaultBranch : BranchSpec()
        public object AllBranches : BranchSpec()
        public class SpecificBranch(public val branchName: String) : BranchSpec()
    }

    /**
     * @param finishDate epoch seconds
     */
    public data class SimpleBuild(val buildNumber: String, val commitHash: String, val finishDate: Long)

    public fun getBuilds(
        buildType: String,
        builder: BuildLocator.() -> Unit
    ): Sequence<Build>

    public fun getProjectById(id: ProjectId): Project

    public fun getProjectByBuildConfiguration(id: BuildConfigurationId): Project

    public fun getBuild(buildId: String): Build

    @Deprecated("Use getBuild instead")
    public fun getBuildNumber(buildId: String): String?

    public fun triggerBuild(
        buildType: String,
        comment: String,
        branchName: String,
        parameters: Map<String, String> = emptyMap()
    ): Build

    public companion object {

        public fun create(teamcityCredentials: TeamcityCredentials): TeamcityApi = TeamcityApiImpl(teamcityCredentials)
    }
}
