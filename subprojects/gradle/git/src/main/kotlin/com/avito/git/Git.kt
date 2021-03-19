package com.avito.git

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.utils.ProcessRunner
import java.io.File

interface Git {

    val defaultRemote: String
        get() = "origin"

    fun init(): Result<Unit>

    fun addAll(): Result<Unit>

    fun commit(message: String): Result<Unit>

    fun checkout(branchName: String, create: Boolean): Result<Unit>

    fun addRemote(url: String): Result<Unit>

    fun fetch(remote: String = defaultRemote, commitHash: String?, depth: Int? = null): Result<Unit>

    fun resetHard(revision: String): Result<Unit>

    /**
     * @param abbrevRef Use a non-ambiguous short name of the objects name
     */
    fun tryParseRev(branchName: String, abbrevRef: Boolean = false): Result<String>

    fun config(option: String): Result<String>

    class Impl(
        rootDir: File,
        loggerFactory: LoggerFactory
    ) : Git {

        private val processRunner = ProcessRunner.Real(rootDir, loggerFactory)

        private val logger = loggerFactory.create<Git>()

        override fun init(): Result<Unit> = git("init").map { Unit }

        override fun addAll(): Result<Unit> = git("add --all").map { Unit }

        override fun commit(message: String): Result<Unit> =
            git("commit --author='test <>' --all --message='${escapeGitMessage(message)}'").map { Unit }

        override fun checkout(branchName: String, create: Boolean): Result<Unit> =
            git("checkout ${if (create) "-b" else ""} $branchName").map { Unit }

        override fun addRemote(url: String): Result<Unit> = git("remote add origin $url").map { Unit }

        override fun fetch(remote: String, commitHash: String?, depth: Int?): Result<Unit> =
            git("fetch $remote ${if (depth != null) "--depth=$depth" else ""} $commitHash").map { Unit }

        override fun resetHard(revision: String): Result<Unit> = git("reset --hard $revision").map { Unit }

        override fun config(option: String): Result<String> = git("config $option")

        override fun tryParseRev(branchName: String, abbrevRef: Boolean): Result<String> {
            val abbrevRefOption = if (abbrevRef) " --abbrev-ref" else ""
            return git("rev-parse$abbrevRefOption $branchName")
                .recover { error ->
                    throw IllegalStateException(
                        "Can't get revision for $branchName",
                        error
                    )
                }
        }

        private fun git(command: String): Result<String> =
            processRunner.run(command = "git $command")
                .apply {
                    onFailure { error -> logger.warn("git error running: '$command'", error) }
                }

        private fun escapeGitMessage(message: String) = message.replace("\\s+".toRegex()) { "_" }
    }
}
