package dev1503.pocketlauncher.modloader

import dev1503.pocketlauncher.Log

class ModInfo (
    val name: String,
    val packageName: String,
    val supportedVersions: List<String>,
    val dirPath: String,
    val loader: String,
    val entry: String,
    val entryMethod: String = "") {

    val TAG = "ModInfo"

    val entrySuffix: String
        get() {
            return entry.substringAfterLast(".").lowercase()
        }
    val entryFilePath: String
        get() {
            return dirPath + entry
        }

    init {
        Log.d(TAG, """ModInfo {
            |    name: $name
            |    packageName: $packageName
            |    supportedVersions: $supportedVersions
            |    dirPath: $dirPath
            |    loader: $loader
            |    entry: $entry
            |    entryMethod: $entryMethod
            |}""".trimMargin())
    }

    fun isVersionSupported(version: String): Boolean {
        supportedVersions.forEach { supportedVersion ->
            if (isVersionMatch(version, supportedVersion)) {
                return true
            }
        }
        return false
    }

    private fun isVersionMatch(version: String, pattern: String): Boolean {
        Log.d(TAG, "isVersionMatch: $version, $pattern")
        if (pattern == version) {
            return true
        }
        if (pattern.contains("-")) {
            val range = pattern.split("-")
            if (range.size == 2) {
                val minVersion = range[0].trim()
                val maxVersion = range[1].trim()
                return isVersionInRange(version, minVersion, maxVersion)
            }
        }
        if (pattern.contains("*")) {
            return isVersionMatchWildcard(version, pattern)
        }
        if (pattern.contains("x", ignoreCase = true)) {
            return isVersionMatchX(version, pattern)
        }

        return false
    }

    private fun isVersionInRange(version: String, minVersion: String, maxVersion: String): Boolean {
        val versionParts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = minVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val maxParts = maxVersion.split(".").map { it.toIntOrNull() ?: 0 }

        val isGreaterOrEqual = compareVersion(versionParts, minParts) >= 0
        val isLessOrEqual = compareVersion(versionParts, maxParts) <= 0

        return isGreaterOrEqual && isLessOrEqual
    }

    private fun compareVersion(v1: List<Int>, v2: List<Int>): Int {
        val maxLength = maxOf(v1.size, v2.size)

        for (i in 0 until maxLength) {
            val part1 = v1.getOrElse(i) { 0 }
            val part2 = v2.getOrElse(i) { 0 }

            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        return 0
    }

    private fun isVersionMatchWildcard(version: String, pattern: String): Boolean {
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", ".*")

        return version.matches(Regex("^$regexPattern$"))
    }

    private fun isVersionMatchX(version: String, pattern: String): Boolean {
        val patternParts = pattern.split(".")
        val versionParts = version.split(".")

        if (patternParts.size != versionParts.size) {
            return false
        }

        for (i in patternParts.indices) {
            val patternPart = patternParts[i]
            val versionPart = versionParts[i]

            if (patternPart.equals("x", ignoreCase = true)) {
                val num = versionPart.toIntOrNull()
                if (num == null || num == 0) {
                    return false
                }
            } else if (patternPart.contains("x", ignoreCase = true)) {
                if (!patternPart.endsWith("x", ignoreCase = true)) {
                    return false
                }

                val prefix = patternPart.substringBeforeLast("x", "").substringBeforeLast("X", "")
                if (prefix.isNotEmpty()) {
                    if (!versionPart.startsWith(prefix) || versionPart.length <= prefix.length) {
                        return false
                    }
                    val suffix = versionPart.substring(prefix.length)
                    if (suffix.any { !it.isDigit() }) {
                        return false
                    }
                }
            } else {
                if (patternPart != versionPart) {
                    return false
                }
            }
        }
        return true
    }
}