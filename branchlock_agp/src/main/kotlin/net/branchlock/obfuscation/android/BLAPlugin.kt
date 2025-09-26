package net.branchlock.obfuscation.android

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import net.branchlock.obfuscation.BranchlockExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Plugin which is used to obfuscate Android projects (requires AGP 8.0.0+).
 * Targets release builds only.
 */
open class BLAPlugin : Plugin<Project> {
    companion object; // to ensure compatibility with build.gradle.kts
    override fun apply(project: Project) {
        val extension = project.extensions.create("branchlock", BranchlockExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        val taskProvider = project.tasks.register("branchlockObfuscation", BLATask::class.java) {
            it.extension = extension
        }
        val buildType = if (isDebug()) "debug" else "release"
        val variantSelector = androidComponents.selector().withBuildType(buildType)

        println("Registering Branchlock on build type variant \"$buildType\".")

        // there is no better way to do this. If we transformed on scope "PROJECT" and inject "ALL" into the task,
        // we would get a circular dependency error.
        androidComponents.onVariants(variantSelector) { variant ->
            variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                .use(taskProvider)
                .toGet(
                    ScopedArtifact.CLASSES,
                    BLATask::projectJars,
                    BLATask::projectDirs
                )
            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    BLATask::allJars,
                    BLATask::allDirs,
                    BLATask::allOutput,
                )
        }
    }

    open fun isDebug(): Boolean {
        return false
    }
}