package dev1503.pocketlauncher.modloader

import android.app.Activity
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import java.io.File
import java.lang.reflect.Field

class ModLoader(val activity: Activity, val classLoader: ClassLoader) {
    val TAG = "ModLoader"
    private var modIndex = 0
    private var isLoading = false

    private val loadResults = mutableListOf<Pair<Boolean, String>>()
    private val errorStack = mutableListOf<Pair<String, String>>()

    fun loadMods(
        mods: List<ModInfo>,
        instanceInfo: InstanceInfo,
        progressCallback: (ModInfo, Int) -> Unit,
        finishCallback: (Boolean, String) -> Unit
    ) {
        if (isLoading) {
            finishCallback(false, "Already loading mods")
            return
        }

        isLoading = true
        modIndex = 0
        loadResults.clear()
        errorStack.clear()

        if (mods.isEmpty()) {
            finishCallback(true, "No mods to load")
            isLoading = false
            return
        }

        fun loadNextMod() {
            if (modIndex >= mods.size) {
                val allSuccess = loadResults.all { it.first }
                val successCount = loadResults.count { it.first }
                val failCount = loadResults.size - successCount

                val message = if (allSuccess) {
                    "All ${mods.size} mods loaded successfully"
                } else {
                    val errorDetails = buildString {
                        append("Loaded $successCount/${mods.size} mods, $failCount failed")

                        if (errorStack.isNotEmpty()) {
                            append("\n\nFailed mods:")
                            for ((modName, error) in errorStack) {
                                append("\n\n[${modName}]:\n$error")
                            }
                        }
                    }
                    errorDetails
                }

                finishCallback(allSuccess, message)
                isLoading = false
                return
            }

            val mod = mods[modIndex]
            val currentIndex = modIndex
            progressCallback(mod, 0)

            loadMod(mod, instanceInfo) { success, message ->
                loadResults.add(Pair(success, message))

                if (!success) {
                    val errorDetails = buildString {
                        append("Mod: ${mod.name}\n")
                        append("Error: $message")
                    }
                    errorStack.add(Pair(mod.name, errorDetails))
                }

                if (success) {
                    Log.i(TAG, "Mod loaded successfully: ${mod.name}")
                } else {
                    Log.e(TAG, "Failed to load mod ${mod.name}: $message")
                }

                modIndex++
                loadNextMod()
            }
        }

        loadNextMod()
    }

    fun loadMod(mod: ModInfo, instanceInfo: InstanceInfo, finishCallback: (Boolean, String) -> Unit) {
        if (!mod.isVersionSupported(instanceInfo.versionName)) {
            finishCallback(false, activity.getString(R.string.mod_not_supported_version, mod.name))
            return
        }
        Log.d(TAG, "Loading mod: ${mod.name}")
        try {
            if (mod.loader == "dev1503.pocketlauncher") {
                if (mod.entrySuffix == "dex") {
                    val pathListField: Field =
                        classLoader.javaClass.getSuperclass().getDeclaredField("pathList")
                    pathListField.setAccessible(true)
                    val pathList: Any = pathListField.get(classLoader)!!

                    val addDexPath = pathList.javaClass.getDeclaredMethod(
                        "addDexPath",
                        String::class.java,
                        File::class.java
                    )
                    addDexPath.invoke(pathList, mod.entryFilePath, null)

                    val entryClass = mod.entryMethod.split("#")[0]
                    val entryMethod = mod.entryMethod.split("#")[1]
                    val entryClassObj = classLoader.loadClass(entryClass)
                    val entryMethodObj = entryClassObj.getDeclaredMethod(entryMethod, Activity::class.java, ModInfo::class.java)

                    entryMethodObj.invoke(null, activity, mod)
                    finishCallback(true, "Mod loaded successfully: ${mod.name}")
                } else {
                    finishCallback(false, "Unsupported entry suffix: ${mod.entrySuffix}")
                }
            } else {
                finishCallback(false, "Unsupported loader: ${mod.loader}")
            }
        } catch (e: Exception) {
            val errorMsg = Log.getStackTraceString(e)
            finishCallback(false, "Failed to load mod ${mod.name}: $errorMsg")
            Log.e(TAG, "loadMod: ${mod.name}", e)
        }
    }
}