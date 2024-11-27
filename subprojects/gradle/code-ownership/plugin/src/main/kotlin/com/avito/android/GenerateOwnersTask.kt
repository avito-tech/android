package com.avito.android

import com.avito.android.model.AvitoCodeOwner
import com.avito.android.model.Owner
import com.avito.android.model.Team
import com.avito.android.model.Type
import com.avito.android.model.Unit
import com.avito.android.model.network.AvitoOwner
import com.avito.android.model.network.AvitoOwnersClient
import com.avito.android.model.network.OwnerType
import com.avito.android.utils.cyrillicToLatinAlphabet
import com.avito.utils.ProcessRunner
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.Duration
import java.util.Locale

@CacheableTask
internal abstract class GenerateOwnersTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val moduleDir: DirectoryProperty

    @get:Internal
    internal abstract val avitoOwnersClient: Property<AvitoOwnersClient>

    @get:Input
    internal abstract val modulePathToOwners: MapProperty<String, Set<Owner>>

    @get:OutputFile
    internal abstract val bitbucketCodeOwnershipFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val bitbucketCodeOwnershipExclusionsFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val moduleDir = moduleDir.orNull?.asFile
        if (moduleDir == null || !moduleDir.exists()) {
            error(
                "The directory of the module for owners generation does not exist. Input: $moduleDir. " +
                    "Please, setup correct Directory object. " +
                    "For example \"project(\":common:code-owners\").layout.projectDirectory.\""
            )
        }

        val remoteOwners = getRemoteOwners()

        val ownersClassName = ClassName("com.avito.android.ownership", "Owners")

        val typeClassName = Type::class.asClassName()
        val unitClassName = Unit::class.asClassName()
        val teamClassName = Team::class.asClassName()

        val ownersEnum = createOwnersEnumBuilder(ownersClassName, typeClassName)
            .addRemoteOwnersToEnum(remoteOwners, unitClassName, teamClassName)
            .build()

        writeIntoFile(
            fileSpec = FileSpec.builder(ownersClassName.packageName, ownersClassName.simpleName)
                .addImport(unitClassName.packageName, unitClassName.simpleName)
                .addImport(teamClassName.packageName, teamClassName.simpleName)
                .indent("    ")
                .addType(ownersEnum)
                .build(),
            moduleDir = moduleDir
        )

        generateCodeOwnershipFile(remoteOwners)
    }

    private fun generateCodeOwnershipFile(remoteOwners: List<AvitoOwner>) {
        val exclusions = getBitbucketOwnersExclusions()
        val moduleToEmailPairs = modulePathToOwners.get().map { mapEntry ->
            val modulePath = mapEntry.key.replace(":", "/")
            val moduleOwners = mapEntry.value
                .filterIsInstance<AvitoCodeOwner>()
                .filter {
                    !exclusions.getOrDefault(modulePath.trim('/'), emptyList())
                        .contains(it.type.id)
                }

            val emails = moduleOwners.flatMap { localOwner ->
                findRemoteOwnerByIdRecursive(localOwner.type.id, remoteOwners)?.let { remoteOwner ->
                    (remoteOwner.children + remoteOwner).flatMap { it.people }.map { it.email }
                } ?: setOf()
            }
                .toSet()
                .sorted()
            modulePath to emails
        }
        writeIntoCodeOwnersFile(moduleToEmailPairs)
    }

    private fun getBitbucketOwnersExclusions(): Map<String, List<String>> {
        val exclusionsFile = bitbucketCodeOwnershipExclusionsFile.get().asFile
        return if (exclusionsFile.exists()) {
            val csvLines = exclusionsFile.readCsvLines()
            validateCsvStructure(columns = csvLines.first())
            csvLines.drop(1).fold(mutableMapOf()) { map, fields ->
                val (module, ownerIds) = fields
                map[module.trim('/')] = ownerIds.split(" ")
                map
            }
        } else {
            emptyMap()
        }
    }

    private fun File.readCsvLines(): List<List<String>> =
        CsvMapper().readerForListOf(String::class.java)
            .with(CsvParser.Feature.WRAP_AS_ARRAY)
            .readValues<List<String>>(this)
            .readAll()

    private fun validateCsvStructure(columns: List<String>) {
        val (module, ownerIds) = columns
        assert(module == MODULE_FIELD)
        assert(ownerIds == OWNER_IDS_FIELD)
    }

    private fun findRemoteOwnerByIdRecursive(ownerId: String, owners: List<AvitoOwner>): AvitoOwner? {
        owners.forEach { owner ->
            if (owner.id == ownerId) {
                return owner
            }
            findRemoteOwnerByIdRecursive(ownerId, owner.children)?.let { ownerFoundInChildren ->
                return ownerFoundInChildren
            }
        }
        return null
    }

    private fun writeIntoCodeOwnersFile(pairs: List<Pair<String, List<String>>>) {
        val file = bitbucketCodeOwnershipFile.get().asFile

        file.writeText(
            pairs.joinToString("\n") { pair ->
                "${pair.first}/ ${pair.second.joinToString(" ")}"
            }
        )
    }

    private fun TypeSpec.Builder.addRemoteOwnersToEnum(
        remoteOwners: List<AvitoOwner>,
        unitClassName: ClassName,
        teamClassName: ClassName
    ): TypeSpec.Builder {
        val allUnits = mutableListOf<String>()
        val allTeams = mutableListOf<String>()

        remoteOwners.forEachIndexed { _, owner ->
            if (owner.type == OwnerType.Unit) {
                val originalUnitName = owner.name.normalizeName()
                val finalUnitName = originalUnitName + "_Unit"

                allUnits.add(finalUnitName)

                val unitEnumParamsCode = CodeBlock.builder()
                    .unindent()
                    .addStatement(
                        """
                            
                            %1L(
                                name = %2S,
                                id = %3S
                            ),
                            chatChannels = setOf(%4L)
                            """.trimIndent(),
                        unitClassName.simpleName,
                        owner.name,
                        owner.id,
                        owner.channels.joinToString(separator = ", ", transform = { "\"$it\"" })
                    )
                    .indent()

                addEnumConstant(
                    finalUnitName,
                    TypeSpec.anonymousClassBuilder()
                        .addSuperclassConstructorParameter(unitEnumParamsCode.build())
                        .build()
                )

                owner.children.map { child ->
                    val originalTeamName = child.name.normalizeName()
                    val finalTeamName =
                        if (originalTeamName in allTeams) {
                            "$originalUnitName$originalTeamName"
                        } else {
                            originalTeamName
                        }
                    allTeams.add(finalTeamName)

                    val teamEnumParamsCode = CodeBlock.builder()
                        .unindent()
                        .addStatement(
                            """
                                
                                %1L(
                                    name = %2S,
                                    id = %3S,
                                    unit = %4L,
                                ),
                                chatChannels = setOf(%5L)
                            """.trimIndent(),
                            teamClassName.simpleName,
                            child.name,
                            child.id,
                            finalUnitName,
                            child.channels.joinToString(separator = ", ", transform = { "\"$it\"" })
                        )
                        .indent()

                    addEnumConstant(
                        finalTeamName + "_Team",
                        TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter(teamEnumParamsCode.build())
                            .build()
                    )
                }
            }
        }

        return this
    }

    private fun getRemoteOwners() = avitoOwnersClient.get().getAvitoOwners()

    private fun createOwnersEnumBuilder(ownersClassName: ClassName, typeClassName: ClassName): TypeSpec.Builder {
        val ownerTypeParam = ParameterSpec.builder(OWNER_TYPE, typeClassName)
        val chatChannelsType = Set::class.asClassName()
            .parameterizedBy(String::class.asClassName())

        val ownersEnum = TypeSpec.enumBuilder(ownersClassName)
            .addKdoc(COMMENT)
            .addSuperinterface(AvitoCodeOwner::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(ownerTypeParam.build())
                    .addParameter(
                        ParameterSpec.builder(
                            OWNER_CHAT_CHANNELS,
                            chatChannelsType
                        )
                            .defaultValue("setOf()")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(OWNER_TYPE, typeClassName)
                    .initializer(OWNER_TYPE)
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(OWNER_CHAT_CHANNELS, chatChannelsType)
                    .initializer(OWNER_CHAT_CHANNELS)
                    .build()
            )

        return ownersEnum
    }

    private fun writeIntoFile(fileSpec: FileSpec, moduleDir: File) {
        val file = File(moduleDir, "/src/main/kotlin")

        if (!file.exists()) {
            file.mkdirs()
        }

        val owners = File(file, fileSpec.toJavaFileObject().name)

        val doesFileExist = owners.exists()

        fileSpec.writeTo(file)

        if (!doesFileExist) {
            ProcessRunner.create(moduleDir).run(command = "git add ${owners.path}", Duration.ofSeconds(10))
        }
    }

    private fun String.normalizeName(): String {
        val words = replace("&", "_And_")
            .replace("Ƞ", "Eta")
            .replace("Ω", "Omega")
            .replace("\t", "")
            .replace(".", "")
            .replace("(", "")
            .replace(")", "")
            .split(" ", "/", "\\", "_", "-")
        return words.joinToString("_") {
            val sb = StringBuilder()
            it.forEachIndexed { index, c ->
                val currentChar = c.toString()
                val cyrillicChar: String? = cyrillicToLatinAlphabet[currentChar]
                val newChar = cyrillicChar ?: currentChar
                sb.append(if (index == 0) newChar.uppercase(Locale.ROOT) else newChar)
            }
            sb.toString()
        }
    }

    companion object {
        const val MODULE_FIELD = "module"
        const val OWNER_IDS_FIELD = "ownerIds"
        const val OWNER_CHAT_CHANNELS = "chatChannels"
        const val OWNER_TYPE = "type"

        val COMMENT = """
            !!! This file is autogenerated. Do not modify it by hands. !!!
            
            Use {@code ./gradlew generateCodeOwnersFile} command to update the owners.
    """.trimIndent()
    }
}
