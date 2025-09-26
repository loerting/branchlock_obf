package net.branchlock.obfuscation.api

import mjson.Json
import mjson.Json.MalformedJsonException
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import java.io.PrintStream
import java.util.function.Consumer

class BranchlockAPIContacter(
    private val branchlockApiUrl: String,
    private val bearerToken: String,
    private val logger: PrintStream,
    private val pollingInterval: Int
) {
    companion object {
        const val API_NEW_JOB = "/job/new"
        const val API_JOB_STATUS = "/job/%s/status"
        const val API_JOB_DOWNLOAD = "/job/%s/download"
    }

    fun obfuscate(
        projectId: String,
        inputJarFile: File,
        inputLibraries: Collection<File>,
        jarOutput: Consumer<InputStream>
    ): Int {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("input", inputJarFile.name, inputJarFile.asRequestBody())
            .addFormDataPart("project_id", projectId)
            .apply {
                for (inputLib in inputLibraries) {
                    addFormDataPart("libraries[]", inputLib.name, inputLib.asRequestBody())
                }
            }
            .build()

        val request = Request.Builder()
            .url(branchlockApiUrl + API_NEW_JOB)
            .addHeader("Authorization", "Bearer $bearerToken")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Branchlock Gradle Plugin")
            .post(requestBody)
            .build()

        logger.println("Contacting Branchlock API...")

        client.newCall(request).execute().use {
            if (!it.isSuccessful) {
                logger.println("Error: Failed to contact Branchlock API. Response code: ${it.code}")
                logger.println("Response body: ${it.body?.string()}")
                return 1
            }

            if (it.body == null || it.body?.contentLength() == 0L) {
                logger.println("Error: Branchlock API returned empty body.")
                return 9
            }

            val bodyString = it.body?.string()
            val jsonResult = try {
            Json.read(bodyString)
            } catch (e: MalformedJsonException) {
                e.printStackTrace()
                logger.println("Error: Failed to parse JSON response from Branchlock API.")
                logger.println("Response body: $bodyString")
                return 11
            }

            val message = jsonResult.at("message").asString()
            val status = jsonResult.at("status").asString().lowercase()

            logger.println("API returned status \"$status\" with message: \"$message\"")

            if (status != "success") {
                return 2
            }

            logger.println("Waiting for obfuscation to finish...")
            logger.println("Watch the live progress by viewing the project online.")
            return pollUntilFinish(projectId, client, jarOutput)
        }
    }

    private fun pollUntilFinish(projectId: String, client: OkHttpClient, jarOutput: Consumer<InputStream>): Int {
        var finished = false
        lateinit var downloadUrl: String
        var failures = 0
        while (!finished) {
            Thread.sleep(pollingInterval.toLong())
            val statusRequest = Request.Builder()
                .url(branchlockApiUrl + API_JOB_STATUS.format(projectId))
                .addHeader("Authorization", "Bearer $bearerToken")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Branchlock Gradle Plugin")
                .get()
                .build()

            client.newCall(statusRequest).execute().use {
                if (!it.isSuccessful) {
                    failures++
                    logger.println("Error: Failed to contact Branchlock API while polling (${failures} / 5). Response code: ${it.code}")
                    logger.println("Response body: ${it.body?.string()}")
                    if (failures >= 5) {
                        logger.println("Error: Failed to contact Branchlock API too many times. Aborting.")
                        return 3
                    }
                    return@use
                }

                if (it.body == null || it.body?.contentLength() == 0L) {
                    logger.println("Error: Branchlock API returned empty body.")
                    return 9
                }

                val bodyString = it.body?.string()
                val statusJson = try {
                    Json.read(bodyString)
                } catch (e: MalformedJsonException) {
                    e.printStackTrace()
                    logger.println("Error: Failed to parse JSON response from Branchlock API.")
                    logger.println("Response body: $bodyString")
                    return 11
                }

                val pollingStatus = statusJson.at("status").asString().lowercase()
                if (pollingStatus == "completed") {
                    finished = true
                    downloadUrl = statusJson.at("download_url").asString()
                    val outputLog = statusJson.at("log").asString()
                    logger.println("Obfuscation finished.")

                    logger.println()
                    logger.println(outputLog)
                    logger.println()

                    // also write to log file
                    val logFile = File("branchlock.log")
                    logFile.writeText(outputLog)

                } else if (pollingStatus != "running" && pollingStatus != "success") {
                    logger.println("Error: Obfuscation failed. Status: $pollingStatus")
                    return 4
                }
            }
        }

        logger.println("Job finished. Downloading result from $downloadUrl.")

        val downloadRequest = Request.Builder()
            .url(downloadUrl)
            .addHeader("Authorization", "Bearer $bearerToken")
            .addHeader("User-Agent", "Branchlock Gradle Plugin")
            .get()
            .build()

        client.newCall(downloadRequest).execute().use {
            if (!it.isSuccessful) {
                logger.println("Error: Failed to download obfuscated jar. Response code: ${it.code}")
                return 5
            }

            if (it.body == null || it.body?.contentLength() == 0L) {
                logger.println("Error: Branchlock API returned empty body.")
                return 9
            }

            val byteStream = it.body?.byteStream()

            if (byteStream == null) {
                logger.println("Error: Failed to get byte stream from response.")
                return 6
            }

            jarOutput.accept(byteStream)
            return 0
        }
    }
}