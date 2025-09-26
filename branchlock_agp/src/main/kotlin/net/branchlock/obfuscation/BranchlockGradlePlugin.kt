package net.branchlock.obfuscation

import java.util.*

class BranchlockGradlePlugin {
    fun logInfo() {
        println("Branchlock Gradle Plugin ${getVersion()}.")
        println("Copyright (c) ${Calendar.getInstance().get(Calendar.YEAR)} branchlock.net. All rights reserved.")
        println()
    }
    private fun getVersion(): String {
        return javaClass.`package`.implementationVersion ?: "[unknown version]"
    }
}