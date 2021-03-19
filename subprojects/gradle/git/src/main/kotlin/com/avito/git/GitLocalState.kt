package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.logger.LoggerFactory
import org.gradle.api.Project
import java.io.File

interface GitLocalState : GitState

class GitLocalStateImpl(
    rootDir: File,
    targetBranch: String?,
    loggerFactory: LoggerFactory
) : GitLocalState {

    override val defaultBranch = "develop"

    override val originalBranch: Branch

    override val currentBranch: Branch

    override val targetBranch: Branch?

    init {
        @Suppress("NAME_SHADOWING")
        val targetBranch = targetBranch?.asBranchWithoutOrigin()

        val git = Git.Impl(
            rootDir = rootDir,
            loggerFactory = loggerFactory
        )

        val gitBranch: String = git.tryParseRev("HEAD", abbrevRef = true).getOrThrow()
        val gitCommit: String = git.tryParseRev("HEAD", abbrevRef = false).getOrThrow()

        this.currentBranch = Branch(
            name = gitBranch,
            commit = gitCommit
        )

        this.originalBranch = currentBranch

        var target: Branch? = null

        if (!targetBranch.isNullOrBlank()) {

            git.tryParseRev(targetBranch)
                .onSuccess { targetCommit ->
                    target = Branch(
                        name = targetBranch,
                        commit = targetCommit
                    )
                }
        }

        this.targetBranch = target
    }

    companion object {

        fun from(project: Project, loggerFactory: LoggerFactory): GitState {
            val targetBranch: String? = project.getOptionalStringProperty("targetBranch")
            return GitLocalStateImpl(
                rootDir = project.rootDir,
                targetBranch = targetBranch,
                loggerFactory = loggerFactory
            )
        }
    }
}
