package dev1503.pocketlauncher.modloader

import android.os.Bundle
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.dexbridge.MinecraftActivity
import dev1503.pocketlauncher.mod.events.AfterMinecraftActivityOnCreateListener
import dev1503.pocketlauncher.mod.events.EventListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetExternalStoragePathListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityHasWriteExternalStoragePermissionListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityOnCreateListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityOnDestroyListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityRequestStoragePermissionListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityStaticInitListener

class ModEventListener(val modInfo: ModInfo, val listener: EventListener) {
    companion object {
        private const val TAG = "ModEventListener"

        private val listeners = mutableListOf<ModEventListener>()
        private val eventResults = mutableMapOf<String, Any?>()

        fun addListener(listener: ModEventListener): Boolean {
            return listeners.add(listener)
        }

        fun invoke(eventName: String, vararg args: Any): Any? {
            val lowerEventName = eventName.lowercase()
            var hasMatchingListener = false

            listeners.forEach { modListener ->
                if (modListener.listener::class.java.interfaces[0].simpleName.lowercase() == lowerEventName + "listener") {
                    hasMatchingListener = true
                    val lastResult = eventResults[lowerEventName]
                    when (lowerEventName) {
                        "afterminecraftactivityoncreate" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as AfterMinecraftActivityOnCreateListener).onCreate(
                                    args[0] as MinecraftActivity,
                                    args[1] as Bundle?,
                                    (lastResult as? Boolean) == true
                                )
                        }
                        "onminecraftactivitygetexternalstoragepath" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityGetExternalStoragePathListener).getExternalStoragePath(
                                    args[0] as MinecraftActivity,
                                    args[1] as String,
                                    args[2] as String,
                                    lastResult as? String
                                )
                        }
                        "onminecraftactivityoncreate" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityOnCreateListener).onCreate(
                                    args[0] as MinecraftActivity,
                                    args[1] as Bundle?,
                                    (lastResult as? Boolean) == true
                                )
                        }
                        "onminecraftactivitystaticinit" -> (modListener.listener as OnMinecraftActivityStaticInitListener)._client()
                        "onminecraftactivityondestroy" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityOnDestroyListener).onDestroy(
                                    args[0] as MinecraftActivity,
                                    (lastResult as? Boolean) == true
                                )
                        }
                        "onminecraftactivityhaswriteexternalstoragepermission" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityHasWriteExternalStoragePermissionListener).hasWriteExternalStoragePermission(
                                    args[0] as MinecraftActivity,
                                    args[1] as Boolean,
                                    args[2] as Boolean,
                                    (lastResult as? Boolean) == true
                                )
                        }
                        "onminecraftactivityrequeststoragepermission" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityRequestStoragePermissionListener).requestStoragePermission(
                                    args[0] as MinecraftActivity,
                                    args[1] as Int,
                                    args[2] as Boolean,
                                    (lastResult as? Boolean) == true
                                )
                        }
                    }
                }
            }
            if (hasMatchingListener || (eventResults.containsKey(lowerEventName) && eventResults[lowerEventName] != null)) {
                return eventResults[lowerEventName]
            }
            return null
        }
    }
}