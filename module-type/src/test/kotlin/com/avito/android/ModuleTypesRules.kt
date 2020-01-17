package com.avito.android

import com.avito.android.ModuleType.ANDROID_TEST_LIB
import com.avito.android.ModuleType.COMPONENT_TEST
import com.avito.android.ModuleType.IMPLEMENTATION
import com.avito.android.ModuleType.TEST_LIB
import com.avito.android.ModuleTypesRules.Case.NegativeCase
import com.avito.android.ModuleTypesRules.Case.PositiveCase
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.ManualTempFolder
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.commit
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ModuleTypesRules {

    @TestFactory
    fun cases() = listOf(
        PositiveCase(IMPLEMENTATION, DependencyType.IMPLEMENTATION, IMPLEMENTATION),
        PositiveCase(IMPLEMENTATION, DependencyType.TEST_IMPLEMENTATION, TEST_LIB),
        PositiveCase(IMPLEMENTATION, DependencyType.ANDROID_TEST_IMPLEMENTATION, ANDROID_TEST_LIB),
        PositiveCase(IMPLEMENTATION, DependencyType.ANDROID_TEST_IMPLEMENTATION, TEST_LIB),

        // обычной библиотеке нельзя зависеть от тестовой (чтобы тестовый код не попадал в прод)
        NegativeCase(IMPLEMENTATION, DependencyType.IMPLEMENTATION, TEST_LIB),
        NegativeCase(IMPLEMENTATION, DependencyType.IMPLEMENTATION, ANDROID_TEST_LIB),
        NegativeCase(IMPLEMENTATION, DependencyType.IMPLEMENTATION, COMPONENT_TEST),

        // в конфигурацию для юнит тестов не должны попадать зависимости от instrumentation тестов
        NegativeCase(IMPLEMENTATION, DependencyType.TEST_IMPLEMENTATION, ANDROID_TEST_LIB),
        NegativeCase(IMPLEMENTATION, DependencyType.TEST_IMPLEMENTATION, COMPONENT_TEST),

        PositiveCase(ANDROID_TEST_LIB, DependencyType.IMPLEMENTATION, IMPLEMENTATION),
        PositiveCase(ANDROID_TEST_LIB, DependencyType.IMPLEMENTATION, TEST_LIB),
        PositiveCase(ANDROID_TEST_LIB, DependencyType.IMPLEMENTATION, ANDROID_TEST_LIB),

        NegativeCase(ANDROID_TEST_LIB, DependencyType.IMPLEMENTATION, COMPONENT_TEST),

        PositiveCase(COMPONENT_TEST, DependencyType.IMPLEMENTATION, IMPLEMENTATION),
        PositiveCase(COMPONENT_TEST, DependencyType.IMPLEMENTATION, TEST_LIB),
        PositiveCase(COMPONENT_TEST, DependencyType.IMPLEMENTATION, ANDROID_TEST_LIB),
        PositiveCase(COMPONENT_TEST, DependencyType.IMPLEMENTATION, COMPONENT_TEST)
    )
        .map { case ->
            dynamicTest(case.name) {

                ManualTempFolder.runIn { projectDir ->

                    TestProjectGenerator(
                        plugins = listOf("com.avito.android.impact"),
                        modules = listOf(
                            AndroidLibModule(
                                "feature",
                                plugins = listOf("com.avito.android.module-types"),
                                dependencies = "${case.dependentModuleDependencyType.configuration} project(':dependent_test_module')",
                                buildGradleExtra = if (case.featureModuleType != IMPLEMENTATION) """
                                module {
                                    type com.avito.android.ModuleType.${case.featureModuleType.name}
                                }
                                """.trimIndent() else ""
                            ),
                            AndroidLibModule(
                                "dependent_test_module",
                                plugins = listOf("com.avito.android.module-types"),
                                buildGradleExtra = if (case.dependentModuleType != IMPLEMENTATION) """
                                module {
                                    type com.avito.android.ModuleType.${case.dependentModuleType.name}
                                }
                                """.trimIndent() else ""
                            )
                        )
                    ).generateIn(projectDir)

                    with(projectDir) {
                        git("checkout -b develop")
                    }

                    when (case) {
                        is PositiveCase -> {
                            gradlew(
                                projectDir,
                                ":feature:checkProjectDependenciesType",
                                "-Pavito.moduleTypeValidationEnabled=true"
                            ).assertThat().buildSuccessful()
                        }
                        is NegativeCase -> {
                            gradlew(
                                projectDir,
                                ":feature:checkProjectDependenciesType",
                                "-Pavito.moduleTypeValidationEnabled=true",
                                expectFailure = true
                            ).assertThat()
                                .buildFailed(
                                    "'${case.dependentModuleDependencyType.configuration}' configuration " +
                                        "contains the following ${case.dependentModuleDependencyType.errorSlug} dependencies: :dependent_test_module"
                                )
                        }
                    }
                }
            }
        }

    private sealed class Case(
        val name: String,
        val featureModuleType: ModuleType,
        val dependentModuleDependencyType: DependencyType,
        val dependentModuleType: ModuleType
    ) {

        class PositiveCase(
            featureModuleType: ModuleType,
            dependentModuleDependencyType: DependencyType,
            dependentModuleType: ModuleType
        ) : Case(
            "${featureModuleType.name} ALLOWED to have " +
                "${dependentModuleDependencyType.name} dependency on ${dependentModuleType.name}",
            featureModuleType, dependentModuleDependencyType, dependentModuleType
        )

        class NegativeCase(
            featureModuleType: ModuleType,
            dependentModuleDependencyType: DependencyType,
            dependentModuleType: ModuleType
        ) : Case(
            "${featureModuleType.name} RESTRICTED to have " +
                "${dependentModuleDependencyType.name} dependency on ${dependentModuleType.name}",
            featureModuleType, dependentModuleDependencyType, dependentModuleType
        )
    }

    private enum class DependencyType(val configuration: String, val errorSlug: String) {
        IMPLEMENTATION("implementation", "non-implementation"),
        TEST_IMPLEMENTATION("testImplementation", "non-test"),
        ANDROID_TEST_IMPLEMENTATION("androidTestImplementation", "non-android-test")
    }
}
