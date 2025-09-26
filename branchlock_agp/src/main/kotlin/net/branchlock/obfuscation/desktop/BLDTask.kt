package net.branchlock.obfuscation.desktop

import net.branchlock.obfuscation.BranchlockExtension
import net.branchlock.obfuscation.BranchlockGradlePlugin
import net.branchlock.obfuscation.api.BranchlockHandler
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class BLDTask : DefaultTask() {


    @Internal
    lateinit var outputJar: File

    @Internal
    lateinit var inputJar: File

    @Internal
    lateinit var extension: BranchlockExtension

    @Internal
    lateinit var dependencyJars: Set<File>

    @TaskAction
    fun taskAction() {
        BranchlockGradlePlugin().logInfo()

        val handler = BranchlockHandler(extension)
        handler.validateConfig()

        if (!inputJar.exists()) throw IllegalStateException("Input jar does not exist: $inputJar")
        if (outputJar.exists()) {
            println("Output jar already exists, renaming to .bak")
            outputJar.renameTo(File("${outputJar.absolutePath}.bak"))
        }

        println("Collected ${dependencyJars.size} dependency (library) jars (maximum 100).")

        dependencyJars.find { !it.exists() }?.let {
            throw IllegalStateException("Dependency jar does not exist: $it")
        }

        val stubs = extension.libsToStubs.getOrElse(true) == true
        if (stubs) {
            println("Creating stubs for LIB scope...")
            createStubs(handler, dependencyJars)
        }

        handler.prepareConfigThenContactAPI(inputJar, dependencyJars) { inputStream ->
            // copy bytes from input stream to output stream
            inputStream.copyTo(outputJar.outputStream())
            println("Result saved to ${outputJar.absolutePath}")
        }

        println("Finished Branchlock transformation successfully.")
    }

    private fun createStubs(handler: BranchlockHandler, jars: Set<File>) {
        val newJars = jars.map { jar ->
            val newJar = File.createTempFile("branchlock-lib-stub", ".jar")
            newJar.deleteOnExit()
            val jarStream = JarOutputStream(newJar.outputStream())
            jarStream.use {
                val jarFile = JarFile(jar)
                jarFile.use { file ->
                    file.entries().asSequence().forEach { entry ->
                        if (entry.name.endsWith(".class")) {
                            val stub = handler.classToStub(file.getInputStream(entry))
                            it.putNextEntry(entry)
                            it.write(stub)
                            it.closeEntry()
                        }
                    }
                }
            }
            newJar
        }
        dependencyJars = newJars.toMutableSet()
    }

}
