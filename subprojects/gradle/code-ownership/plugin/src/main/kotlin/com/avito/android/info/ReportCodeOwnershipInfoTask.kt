package com.avito.android.info

import com.avito.android.CodeOwnershipExtension
import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType

public abstract class ReportCodeOwnershipInfoTask : DefaultTask() {

    @get:OutputFile
    public abstract val outputCsv: RegularFileProperty

    @TaskAction
    public fun printOwnership() {
        val file = outputCsv.get().asFile.apply {
            writeText("name,owners\n")
        }

        project.subprojects { subproject ->
            file.appendText(subproject.formatToCsvLine())
        }
    }

    private fun Project.formatToCsvLine(): String {
        val extension = extensions.findByType<CodeOwnershipExtension>()
        val owners = extension?.owners?.orNull ?: emptySet()
        val ownersSerializer = extension?.ownerSerializer?.orNull ?: ToStringOwnerSerializer()
        val ownersCell = owners.joinToString(
            separator = ",",
            prefix = "\"",
            postfix = "\"",
            transform = { owner -> ownersSerializer.serialize(owner) }
        )
        return "$path,$ownersCell\n"
    }

    private class ToStringOwnerSerializer : OwnerSerializer {
        override fun deserialize(rawOwner: String): Owner {
            error("Can't parse $rawOwner to owner entity. This operation is unsupported in ToStringOwnersSerializer")
        }

        override fun serialize(owner: Owner): String {
            return owner.toString()
        }
    }
}
