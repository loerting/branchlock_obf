package net.branchlock.obfuscation.desktop

import net.branchlock.obfuscation.BranchlockExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar


/**
 * Plugin which is used to obfuscate desktop gradle projects.
 * It intercepts release builds and obfuscates them.
 */
open class BLDPlugin : Plugin<Project> {
    companion object; // to ensure compatibility with build.gradle.kts
    override fun apply(project: Project) {
        val extension = project.extensions.create("branchlock", BranchlockExtension::class.java)

        project.afterEvaluate {
            project.tasks.withType(Jar::class.java) { jar ->
                println("Hooking Branchlock to task \"${jar.name}\". Running this task will create an obfuscated jar.")
                val inputJar = jar.archiveFile.get().asFile
                val outputJar = project.file("${inputJar.absolutePath.substringBeforeLast('.')}-obf.jar")

                // get dependency jars
                val dependencyJars = project.configurations.getByName("runtimeClasspath").files.filter { it.name.endsWith(".jar") }.toMutableSet()
                dependencyJars += project.configurations.getByName("compileClasspath").files.filter { it.name.endsWith(".jar") }

                val task = project.tasks.register("branchlockObfuscation" + jar.name.capitalize(), BLDTask::class.java) {
                    it.extension = extension
                    it.inputJar = inputJar
                    it.outputJar = outputJar
                    it.dependencyJars = dependencyJars
                }

                // after jar task run obfuscation task
                jar.finalizedBy(task)
            }
        }
    }
}