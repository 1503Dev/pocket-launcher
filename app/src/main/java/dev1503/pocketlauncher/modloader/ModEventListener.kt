package dev1503.pocketlauncher.modloader

import android.os.Bundle
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.dexbridge.MinecraftActivity
import dev1503.pocketlauncher.mod.events.AfterMinecraftActivityOnCreateListener
import dev1503.pocketlauncher.mod.events.EventListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetExternalStoragePathListener
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityOnCreateListener

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
            val lastResult = eventResults[lowerEventName]
            var hasMatchingListener = false

            listeners.forEach { modListener ->
                if (modListener.listener::class.java.interfaces[0].simpleName.lowercase() == lowerEventName + "listener") {
                    hasMatchingListener = true
                    when (lowerEventName) {
                        "afterminecraftactivityoncreate" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as AfterMinecraftActivityOnCreateListener).onCreate(
                                    args[0] as MinecraftActivity,
                                    args[1] as Bundle?,
                                    lastResult as? Boolean ?: true
                                )
                        }
                        "onminecraftactivitygetexternalstoragepath" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityGetExternalStoragePathListener).getExternalStoragePath(
                                    args[0] as MinecraftActivity,
                                    args[1] as String,
                                    args[2] as String,
                                    lastResult as String?
                                )
                        }
                        "onminecraftactivityoncreate" -> {
                            eventResults[lowerEventName] =
                                (modListener.listener as OnMinecraftActivityOnCreateListener).onCreate(
                                    args[0] as MinecraftActivity,
                                    args[1] as Bundle?,
                                    lastResult as? Boolean ?: true
                                )
                        }
                    }
                }
            }
            return if (hasMatchingListener) eventResults[lowerEventName] else null
        }
    }
}