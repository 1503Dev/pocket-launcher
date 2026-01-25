package dev1503.pocketlauncher

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class KVConfig(
    private val context: Context,
    private val filePath: String
) {

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    val lock = ReentrantReadWriteLock()
    val file: File = File(context.filesDir, filePath)
    val cache: MutableMap<String, Any?> = mutableMapOf()

    init {
        loadFromFile()
    }
    fun <T> set(key: String, value: T?) {
        lock.write {
            if (value == null) {
                cache.remove(key)
            } else {
                cache[key] = value
            }
            saveToFile()
        }
    }
    fun <T> get(key: String, defaultValue: T? = null): T? {
        return lock.read {
            (cache[key] as? T) ?: defaultValue
        }
    }
    fun getString(key: String, defaultValue: String = ""): String {
        return when (val value = cache[key]) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            null -> defaultValue
            else -> value.toString()
        }
    }
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return when (val value = cache[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return when (val value = cache[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: defaultValue
            is Number -> value.toInt() != 0
            else -> defaultValue
        }
    }
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return when (val value = cache[key]) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return when (val value = cache[key]) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return when (val value = cache[key]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun <T> setObject(key: String, obj: T?) {
        lock.write {
            if (obj == null) {
                cache.remove(key)
            } else {
                cache[key] = gson.toJson(obj)
            }
            saveToFile()
        }
    }
    inline fun <reified T> getObject(key: String): T? {
        return lock.read {
            try {
                val json = cache[key] as? String
                json?.let { gson.fromJson(it, T::class.java) }
            } catch (e: Exception) {
                null
            }
        }
    }
    fun remove(key: String) {
        set(key, null)
    }
    fun clear() {
        lock.write {
            cache.clear()
            saveToFile()
        }
    }
    fun contains(key: String): Boolean {
        return lock.read {
            cache.containsKey(key)
        }
    }
    fun keys(): Set<String> {
        return lock.read {
            cache.keys.toSet()
        }
    }
    fun setAll(values: Map<String, Any?>) {
        lock.write {
            cache.putAll(values)
            values.forEach { (key, value) ->
                if (value == null) cache.remove(key)
            }
            saveToFile()
        }
    }
    fun getAll(): Map<String, Any?> {
        return lock.read {
            cache.toMap()
        }
    }
    private fun loadFromFile() {
        if (!file.exists()) return

        try {
            file.bufferedReader().use { reader ->
                val type = object : TypeToken<Map<String, Any?>>() {}.type
                val loaded = gson.fromJson<Map<String, Any?>>(reader, type)
                loaded?.let { cache.putAll(it) }
            }
        } catch (e: Exception) {
            cache.clear()
        }
    }
    private fun saveToFile() {
        try {
            val tempFile = File(file.parent, "${file.name}.tmp")

            tempFile.bufferedWriter().use { writer ->
                gson.toJson(cache, writer)
            }

            if (file.exists()) {
                file.delete()
            }
            tempFile.renameTo(file)
        } catch (e: Exception) {
            throw IOException("Failed to save config", e)
        }
    }
}