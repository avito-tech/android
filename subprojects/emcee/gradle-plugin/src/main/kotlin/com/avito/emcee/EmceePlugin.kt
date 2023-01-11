package com.avito.emcee

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.avito.emcee.internal.EmceeTestTaskConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project

public class EmceePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val emceeExtension = project.extensions.create("emcee", EmceeExtension::class.java)
        emceeExtension.artifactory.folder.set("emcee-transport")
        project.plugins.withType(AppPlugin::class.java) {

            val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->

                val variantSlug = variant.name.uppercase()

                project.tasks.register(
                    "emceeTest$variantSlug",
                    EmceeTestTask::class.java,
                    EmceeTestTaskConfigurator.Builder(emceeExtension)
                        .application(variant)
                        .build()
                ).configure {
                    it.group = "Emcee"
                }
            }
        }
    }
}
