package com.optimizer.s24ultra

import android.app.ActivityManager
import android.content.Context
import java.io.File

class SystemOptimizer(private val context: Context) {

    fun quickOptimize(): String {
        val results = mutableListOf<String>()

        try {
            clearCache()
            results.add("✅ Cache geleert")
        } catch (e: Exception) {
            results.add("⚠️ Cache-Fehler")
        }

        try {
            val tempFiles = File(context.cacheDir.parent + "/code_cache")
            if (tempFiles.exists()) {
                deleteDir(tempFiles)
            }
            results.add("✅ Temp-Dateien bereinigt")
        } catch (e: Exception) {
            results.add("⚠️ Temp-Fehler")
        }

        try {
            val killed = killBackgroundApps()
            results.add("✅ $killed Prozesse beendet")
        } catch (e: Exception) {
            results.add("⚠️ Process-Fehler")
        }

        try {
            System.gc()
            results.add("✅ RAM optimiert")
        } catch (e: Exception) {
            results.add("⚠️ RAM-Fehler")
        }

        return results.joinToString("\n")
    }

    fun clearCache(): Boolean {
        return try {
            deleteDir(context.cacheDir)
            context.cacheDir.mkdirs()
            context.externalCacheDir?.let {
                deleteDir(it)
                it.mkdirs()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun killBackgroundApps(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var killedCount = 0

        val killableApps = listOf(
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.tiktok",
            "com.snapchat.android"
        )

        killableApps.forEach { packageName ->
            try {
                activityManager.killBackgroundProcesses(packageName)
                killedCount++
            } catch (e: Exception) {
                // ok
            }
        }

        return killedCount
    }

    private fun deleteDir(dir: File): Boolean {
        return if (dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                deleteDir(file)
            }
            dir.delete()
        } else {
            dir.delete()
        }
    }
}
