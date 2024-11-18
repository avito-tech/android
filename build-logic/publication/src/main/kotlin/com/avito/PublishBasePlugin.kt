package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PublishBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply("maven-publish")
            group = "com.avito.android"
            version = providers.gradleProperty("projectVersion").get()

            publishing.publications.withType(MavenPublication::class.java) { publication ->
                publication.apply {
                    pom { pom ->
                        pom.apply {
                            name.set("Avito Android Infrastructure")
                            description.set(
                                "Collection of infrastructure libraries and gradle plugins of Avito Android project"
                            )
                            url.set("https://github.com/avito-tech/avito-android")
                            scm { scm ->
                                scm.url.set("https://github.com/avito-tech/avito-android")
                            }
                            licenses {
                                it.license { license ->
                                    license.name.set("MIT License")
                                    license.url.set("https://github.com/avito-tech/avito-android/blob/develop/LICENSE")
                                }
                            }
                            developers { developerSpec ->
                                with(developerSpec) {
                                    developer {
                                        with(it) {
                                            id.set("sboishtyan")
                                            name.set("Sergey Boishtyan")
                                            url.set("https://github.com/sboishtyan")
                                        }
                                    }
                                    developer {
                                        with(it) {
                                            id.set("RuslanMingaliev")
                                            name.set("Ruslan Mingaliev")
                                            url.set("https://github.com/RuslanMingaliev")
                                        }
                                    }
                                    developer {
                                        with(it) {
                                            id.set("Yundin")
                                            name.set("Yundin Vladislav")
                                            url.set("https://github.com/Yundin")
                                        }
                                    }
                                    developer {
                                        with(it) {
                                            id.set("materkey")
                                            name.set("Vyacheslav Kovalev")
                                            url.set("https://github.com/materkey")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
