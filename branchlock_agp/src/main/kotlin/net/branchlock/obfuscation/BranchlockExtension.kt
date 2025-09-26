package net.branchlock.obfuscation

import org.gradle.api.provider.Property

interface BranchlockExtension {
    @Deprecated("Config is stored server-side since 2.0.0")
    val configFile: Property<String?>

    /**
     * The project ID of your project.
     */
    val projectId: Property<String>

    /**
     * The bearer token of your Branchlock account.
     */
    val bearerToken: Property<String>

    /**
     * Whether to upload the library classes to Branchlock.
     * It is recommended to leave this at "true", as it could cause problems if set to "false".
     */
    val uploadLibs: Property<Boolean?>

    /**
     * Whether to upload the libs as stubs instead of the actual libs.
     * This reduces file size and ensures a faster upload.
     * Should be left at "true" and only set to "false" if you encounter problems.
     */
    val libsToStubs: Property<Boolean?>

    /**
     * The interval in milliseconds in which the server will be polled for the obfuscation status.
     */
    val pollingInterval: Property<Int?>

    /**
     * Replacement URL for the Branchlock API. Defaults to "https://branchlock.net/api".
     */
    val branchlockApiUrl: Property<String?>
}

/*
This is how it will look in build.gradle:

buildscript {
    repositories {
        maven { url = "https://branchlock.net/gradle/" }
    }
    dependencies {
        classpath 'net.branchlock:obfuscation:2.0.0'
    }
}

/*
    Do not place this inside the plugins block.
    To test on debug builds use BLADebugPlugin instead.
 */
apply plugin: net.branchlock.obfuscation.android.BLAPlugin

branchlock {
       bearerToken = '' // The bearer token of your Branchlock account
       projectId = '' // The project ID of your Android project
}
 */
