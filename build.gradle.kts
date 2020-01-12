plugins {
    kotlin("jvm")
    `maven-publish`
}

val projectVersion: String by project
val buildToolsVersion: String by project
val javaVersion: String by project
val compileSdkVersion: String by project
val kotlinVersion: String by project
val junit5Version: String by project
val junit5PlatformVersion: String by project
val truthVersion: String by project

allprojects {
    repositories {
        jcenter()
        google()
    }
}

subprojects {

    group = "com.avito.android"
    version = projectVersion

    plugins.withType<MavenPublishPlugin> {
        extensions.getByType<PublishingExtension>().run {
            this.repositories {
                maven {
                    name = "bintray"
                    val bintrayUsername = "avito-tech"
                    val bintrayRepoName = "maven"
                    val bintrayPackageName = "avito-android"
                    setUrl("https://api.bintray.com/maven/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/;publish=0")
                    credentials {
                        username = System.getenv("BINTRAY_USER")
                        password = System.getenv("BINTRAY_API_KEY")
                    }
                }

                maven {
                    name = "artifactory"
                    val artifactoryUrl = System.getenv("ARTIFACTORY_URL")
                    setUrl(artifactoryUrl)
                    credentials {
                        username = System.getenv("ARTIFACTORY_USER")
                        password = System.getenv("ARTIFACTORY_PASSWORD")
                    }
                }
            }
        }
    }

    plugins.withId("kotlin") {

        this@subprojects.tasks {

            compileKotlin {
                kotlinOptions {
                    jvmTarget = javaVersion
                    allWarningsAsErrors = true
                    freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental"
                }
            }

            compileTestKotlin {
                kotlinOptions.jvmTarget = javaVersion
            }

            withType<Test> {
                @Suppress("UnstableApiUsage")
                useJUnitPlatform()

                systemProperty("kotlinVersion", kotlinVersion)
                systemProperty("compileSdkVersion", compileSdkVersion)
                systemProperty("buildToolsVersion", buildToolsVersion)
            }
        }

        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5Version}")

            testRuntimeOnly("org.junit.platform:junit-platform-runner:$junit5PlatformVersion")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junit5PlatformVersion")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

            testImplementation(gradleTestKit())
            testImplementation("com.google.truth:truth:$truthVersion")
        }
    }

    plugins.withId("java-test-fixtures") {

        dependencies {
            "testFixturesImplementation"("org.junit.jupiter:junit-jupiter-api:${junit5Version}")
            "testFixturesImplementation"("com.google.truth:truth:$truthVersion")
        }
    }
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = project.properties["gradleVersion"] as String
    }
    register("setupGitHooks", Exec::class) {
        commandLine("git")
        args("config", "core.hooksPath", ".git_hooks")
    }
}

val initialTasks = project.gradle.startParameter.taskNames
val newTasks = initialTasks + listOf("setupGitHooks")
project.gradle.startParameter.setTaskNames(newTasks)
