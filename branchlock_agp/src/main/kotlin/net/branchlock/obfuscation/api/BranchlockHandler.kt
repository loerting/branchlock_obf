package net.branchlock.obfuscation.api

import net.branchlock.obfuscation.BranchlockExtension
import net.branchlock.obfuscation.asm.StubVisitor
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.InvalidUserDataException
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.InputStream

class BranchlockHandler(val ext: BranchlockExtension) {
    fun prepareConfigThenContactAPI(
        inputFile: File, libraryFiles: Collection<File>,
        function: (t: InputStream) -> Unit
    ) {

        val bearerToken = ext.bearerToken.get()
        val projectId = ext.projectId.get()

        val pollingInterval = ext.pollingInterval.getOrElse(1500)!!.coerceIn(1000, 10000)
        println("Using polling interval of $pollingInterval ms.")

        if (ext.branchlockApiUrl.isPresent) {
            println("Using custom Branchlock API URL: ${ext.branchlockApiUrl.get()}")
        } else {
            println("Using default Branchlock API: https://branchlock.net/api")
        }
        val branchlockApiUrl = ext.branchlockApiUrl.getOrElse("https://branchlock.net/api")!!
        if (branchlockApiUrl.endsWith("/")) {
            throw InvalidUserDataException("Branchlock API URL must not end with a slash.")
        }
        val uploadLibs = ext.uploadLibs.getOrElse(true) == true
        if (!uploadLibs) {
            println("Skipping upload of libraries to Branchlock. This may cause problems with obfuscation.")
        } else {
            println("Uploading libraries to Branchlock... (disable with uploadLibs = false)")
        }

        contactAPI(
            branchlockApiUrl,
            bearerToken,
            projectId,
            inputFile,
            libraryFiles,
            uploadLibs,
            pollingInterval,
            function
        )
    }

    fun contactAPI(
        branchlockApiUrl: String,
        bearerToken: String,
        projectId: String,
        inputFile: File, libraryFiles: Collection<File>,
        uploadLibs: Boolean,
        pollingInterval: Int,
        resultHandler: (t: InputStream) -> Unit
    ) {
        try {
            val apiCode = BranchlockAPIContacter(branchlockApiUrl, bearerToken, System.out, pollingInterval)
                .obfuscate(projectId, inputFile, if (uploadLibs) libraryFiles else emptySet(), resultHandler)
            if (apiCode != 0) {
                throw GradleException("Branchlock API failed with code $apiCode. Check log above.")
            }
        } catch (e: Exception) {
            throw GradleScriptException("Branchlock API failed with exception ${e.message}", e)
        }
    }
    fun validateConfig() {
        if (ext.configFile.isPresent) {
            throw InvalidUserDataException("Please remove the '${BranchlockExtension::configFile.name}' property from your build.gradle file (branchlock block).")
        } else if (!ext.bearerToken.isPresent) {
            throw InvalidUserDataException("Please set the '${BranchlockExtension::bearerToken.name}' property in your build.gradle file (branchlock block).")
        } else if (!ext.projectId.isPresent) {
            throw InvalidUserDataException("Please set the '${BranchlockExtension::projectId.name}' property in your build.gradle file (branchlock block).")
        }
    }

    fun classToStub(inputStream: InputStream): ByteArray {
        val bytes = inputStream.readBytes()
        return try {
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            classReader.accept(StubVisitor(classWriter), ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } catch (e: Exception) {
            println("Error: Failed to convert a class to a stub.")
            bytes
        }
    }
}