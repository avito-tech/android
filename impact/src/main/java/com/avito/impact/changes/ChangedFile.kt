package com.avito.impact.changes

import java.io.File

data class ChangedFile(
    val rootDir: File,
    val file: File,
    val changeType: ChangeType
) {

    init {
        require(file.startsWith(rootDir))
    }

    val relativePath: String = file.toRelativeString(rootDir)
}
