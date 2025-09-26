package net.branchlock.obfuscation.android

/**
 * Plugin which is used to obfuscate Android projects (requires AGP 8.0.0+).
 * Targets debug builds only.
 */
class BLADebugPlugin : BLAPlugin() {
    companion object; // to ensure compatibility with build.gradle.kts
    override fun isDebug(): Boolean {
        return true
    }
}