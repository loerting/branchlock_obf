package net.branchlock.obfuscation.android

import net.branchlock.obfuscation.BranchlockExtension
import net.branchlock.obfuscation.BranchlockGradlePlugin
import net.branchlock.obfuscation.api.BranchlockHandler
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

/**
 * Sample code here https://github.com/android/gradle-recipes/blob/agp-7.4/Kotlin/modifyProjectClasses/app/build.gradle.kts
 *
 * We are transforming on scope "ALL", and the project scope is injected into the task.
 */
abstract class BLATask : DefaultTask() {

    @Internal
    lateinit var extension: BranchlockExtension

    @get:InputFiles
    abstract val projectJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val projectDirs: ListProperty<Directory>


    @get:OutputFile
    abstract val allOutput: RegularFileProperty

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirs: ListProperty<Directory>

    @TaskAction
    fun taskAction() {
        BranchlockGradlePlugin().logInfo()

        val handler = BranchlockHandler(extension)

        handler.validateConfig()

        val allOutputFile = allOutput.get().asFile
        if (allOutputFile.isDirectory) {
            throw UnsupportedOperationException("Output file is a directory. This case is not supported.")
        }
        allOutputFile.parentFile.mkdirs()
        val allOutputJar = JarOutputStream(allOutputFile.outputStream().buffered())
        allOutputJar.use { jarOutputStream ->
            val inputAndLibs = prepareInputAndLibsAndCopyLibsToOutput(handler, allOutputJar)

            // in our case we only have one jar file with all libraries merged into it.
            handler.prepareConfigThenContactAPI(inputAndLibs.first, setOf(inputAndLibs.second)) { inputStr ->
                println("Copying transformed PROJECT scope to output container...")
                JarInputStream(inputStr).use { jis ->
                    // here, no duplicate should occur, as we excluded everything in the project scope from the libs
                    // in #prepareInputAndLibsAndCopyLibsToOutput
                    while (true) {
                        val entry = jis.nextEntry ?: break
                        jarOutputStream.putNextEntry(entry)
                        jis.copyTo(jarOutputStream)
                        jarOutputStream.closeEntry()
                    }
                }
            }
        }
        println("Finished Branchlock transformation successfully.")
    }

    private fun prepareInputAndLibsAndCopyLibsToOutput(handler: BranchlockHandler, allOutputJar: JarOutputStream): Pair<File, File> {
        val inputJarFile = File.createTempFile("branchlock-input", ".jar")
        inputJarFile.deleteOnExit()

        val inputLibraries = File.createTempFile("branchlock-libs", ".jar")
        inputLibraries.deleteOnExit()

        val inputJarOutputStream = JarOutputStream(inputJarFile.outputStream().buffered())
        val inputLibrariesOutputStream = JarOutputStream(inputLibraries.outputStream().buffered())

        inputJarOutputStream.use {
            inputLibrariesOutputStream.use {
                // add all files from the "PROJECT" scope to the input jar.
                println("Merging PROJECT scope into input container...")
                val existingProjectEntries = mutableSetOf<String>()
                iterateOverJarsAndDirs(projectJars, projectDirs) { name, inputStream ->
                    if (existingProjectEntries.contains(name)) return@iterateOverJarsAndDirs
                    existingProjectEntries.add(name)
                    inputJarOutputStream.putNextEntry(JarEntry(name))
                    inputStream.copyTo(inputJarOutputStream)
                    inputJarOutputStream.closeEntry()
                }

                // add all files from the "ALL" scope that are not in the "PROJECT" scope as stubs to the libraries jar.
                val stubs = extension.libsToStubs.getOrElse(true) == true
                println("Merging LIB scope into libraries container...")
                if (stubs) {
                    println("Creating stubs for LIB scope...")
                }
                val existingLibrariesEntries = mutableSetOf<String>()
                iterateOverJarsAndDirs(allJars, allDirs) { name, inputStream ->
                    if (existingProjectEntries.contains(name)) return@iterateOverJarsAndDirs
                    if (existingLibrariesEntries.contains(name)) return@iterateOverJarsAndDirs
                    existingLibrariesEntries.add(name)
                    inputLibrariesOutputStream.putNextEntry(JarEntry(name))
                    if (stubs && name.endsWith(".class")) {
                        inputLibrariesOutputStream.write(handler.classToStub(inputStream))
                    } else {
                        inputStream.copyTo(inputLibrariesOutputStream)
                    }
                    inputLibrariesOutputStream.closeEntry()
                }

                // add all files from the "ALL" scope that are not in the "PROJECT" scope to the output jar.
                println("Copying LIB scope to output container...")
                val existingOutputEntries = mutableSetOf<String>()
                iterateOverJarsAndDirs(allJars, allDirs) { name, inputStream ->
                    if (existingProjectEntries.contains(name)) return@iterateOverJarsAndDirs
                    if (existingOutputEntries.contains(name)) return@iterateOverJarsAndDirs
                    existingOutputEntries.add(name)
                    allOutputJar.putNextEntry(JarEntry(name))
                    inputStream.copyTo(allOutputJar)
                    allOutputJar.closeEntry()
                }
            }
        }
        println("Finished merging containers.")
        return Pair(inputJarFile, inputLibraries)
    }

    private fun iterateOverJarsAndDirs(
        files: ListProperty<RegularFile>,
        dirs: ListProperty<Directory>,
        fileHandler: (name: String, t: InputStream) -> Unit
    ) {
        val jarFiles = files.get()
        val dirFiles = dirs.get()
        jarFiles.forEach { file ->
            val jarFile = JarFile(file.asFile)
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) continue
                fileHandler(entry.name, jarFile.getInputStream(entry))
            }
        }
        dirFiles.forEach { dir ->
            dir.asFile.walk().forEach { file ->
                if (file.isDirectory) return@forEach
                fileHandler(file.relativeTo(dir.asFile).path, file.inputStream())
            }
        }
    }
}