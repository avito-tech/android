package com.avito.android

import com.avito.android.check.deps.CheckExternalDepsCodeOwners
import com.avito.android.check.ownersip.CheckOwnersPresentTask
import com.avito.android.diff.ReportCodeOwnershipDiffTask
import com.avito.android.diff.ReportCodeOwnershipExtension
import com.avito.android.diff.comparator.AvitoModuleOwnerNameComparator
import com.avito.android.diff.formatter.ChatMessageFormatter
import com.avito.android.diff.report.AlertinoOwnerDiffReporter
import com.avito.android.diff.report.OwnersDiffReportDestination
import com.avito.android.info.ExportExternalDepsCodeOwners
import com.avito.android.info.ExportInternalDepsCodeOwners
import com.avito.android.providers.RemoteAvitoModuleOwnersProvider
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class CodeOwnershipPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val codeOwnershipExtension = target.extensions.create<CodeOwnershipExtension>("ownership")
        if (target.isRoot()) {
            val reportExtension = target.extensions.create<ReportCodeOwnershipExtension>("codeOwnershipDiffReport")
            reportExtension.configure(target, codeOwnershipExtension)

            configureDiffReportTask(target, reportExtension)
            configureCheckExternalDepsTask(target, codeOwnershipExtension, reportExtension)
            configureExportInternalDepsTask(target, codeOwnershipExtension)
            configureExportExternalDepsTask(target, codeOwnershipExtension)
            registerOwnershipTask(target, codeOwnershipExtension)
        } else {
            configureStrictOwnershipCheckTask(target, codeOwnershipExtension)
            configureOwnershipTask(target, codeOwnershipExtension)
        }
    }

    private fun ReportCodeOwnershipExtension.configure(
        target: Project,
        codeOwnershipExtension: CodeOwnershipExtension,
    ) {
        val chatMessageFormatter = ChatMessageFormatter()
        actualOwnersProvider.set(
            target.provider {
                RemoteAvitoModuleOwnersProvider(codeOwnershipExtension.avitoOwnersClient.get())
            }
        )
        messageFormatter.set(chatMessageFormatter)
        diffReportDestination.set(
            target.provider {
                OwnersDiffReportDestination.Custom(
                    AlertinoOwnerDiffReporter(
                        alertinoSender = codeOwnershipExtension.alertinoSender.get(),
                        messageFormatter = chatMessageFormatter,
                    ),
                )
            }
        )
        comparator.set(AvitoModuleOwnerNameComparator())
    }

    private fun registerOwnershipTask(
        target: Project,
        codeOwnershipExtension: CodeOwnershipExtension,
    ) = with(target) {
        tasks.register<GenerateOwnersTask>("generateCodeOwnersFile") {
            moduleDir.set(codeOwnershipExtension.ownersDir)
            avitoOwnersClient.set(codeOwnershipExtension.avitoOwnersClient)
            bitbucketCodeOwnershipFile.set(project.layout.projectDirectory.dir(".bitbucket").file("CODEOWNERS"))
            bitbucketCodeOwnershipExclusionsFile.set(
                project.layout.projectDirectory.dir(".bitbucket").file("CodeOwnersExclusions.csv")
            )
        }
    }

    private fun configureOwnershipTask(
        target: Project,
        codeOwnershipExtension: CodeOwnershipExtension,
    ) {
        target.rootProject.tasks.withType(GenerateOwnersTask::class.java).configureEach {
            it.modulePathToOwners.put(target.path, codeOwnershipExtension.owners)
        }
    }

    private fun configureStrictOwnershipCheckTask(target: Project, codeOwnershipExtension: CodeOwnershipExtension) {
        val strictOwnership = target.getBooleanProperty("avito.ownership.strictOwnership", false)
        if (!strictOwnership) return

        target.tasks.register<CheckOwnersPresentTask>("checkOwnersPresent") {
            group = "verification"
            description = "Checks that list of owners is not empty"

            owners.set(codeOwnershipExtension.owners)
            emptyOwnersErrorMessage.set(codeOwnershipExtension.emptyOwnersErrorMessage)
            projectPath.set(target.path)
        }
    }

    private fun configureDiffReportTask(target: Project, reportExtension: ReportCodeOwnershipExtension) {
        target.tasks.register<ReportCodeOwnershipDiffTask>("reportCodeOwnershipDiff") {
            group = "verification"
            description = "Submits report if actual code owners are different from expected ones"

            expectedOwnersProvider.set(reportExtension.expectedOwnersProvider)
            actualOwnersProvider.set(reportExtension.actualOwnersProvider)
            messageFormatter.set(reportExtension.messageFormatter)
            diffReportDestination.set(reportExtension.diffReportDestination)
            comparator.set(reportExtension.comparator)
        }
    }

    private fun configureExportInternalDepsTask(target: Project, codeOwnershipExtension: CodeOwnershipExtension) {
        target.tasks.register<ExportInternalDepsCodeOwners>(ExportInternalDepsCodeOwners.NAME) {
            group = "documentation"
            description = "Exports code owners for all modules to JSON file"

            ownerSerializer.set(codeOwnershipExtension.ownerSerializersProvider)
            outputFile.set(target.layout.buildDirectory.file("ownership/internal-dependencies-owners.json"))
        }
    }

    private fun configureExportExternalDepsTask(target: Project, codeOwnershipExtension: CodeOwnershipExtension) {
        target.tasks.register<ExportExternalDepsCodeOwners>(ExportExternalDepsCodeOwners.NAME) {
            group = "documentation"
            description = "Exports code ownership info for external dependencies to JSON file"

            libsVersionsFile.set(codeOwnershipExtension.externalDependencies.libsVersionsFile)
            libsOwnersFile.set(codeOwnershipExtension.externalDependencies.libsOwnersFile)
            ownerSerializer.set(codeOwnershipExtension.ownerSerializersProvider)
            outputFile.set(target.layout.buildDirectory.file("ownership/external-dependencies-owners.json"))
        }
    }

    private fun configureCheckExternalDepsTask(
        target: Project,
        codeOwnershipExtension: CodeOwnershipExtension,
        reportExtension: ReportCodeOwnershipExtension
    ) {
        target.tasks.register<CheckExternalDepsCodeOwners>(CheckExternalDepsCodeOwners.NAME) {
            group = "verification"
            description = "Checks that all external dependencies has an owners"

            libsVersionsFile.set(codeOwnershipExtension.externalDependencies.libsVersionsFile)
            libsOwnersFile.set(codeOwnershipExtension.externalDependencies.libsOwnersFile)
            expectedOwnersProvider.set(reportExtension.expectedOwnersProvider.get())
            ownerSerializer.set(codeOwnershipExtension.ownerSerializersProvider)
            reportFile.set(target.layout.buildDirectory.file("reports/check_external_dependencies.report"))
        }
    }
}
