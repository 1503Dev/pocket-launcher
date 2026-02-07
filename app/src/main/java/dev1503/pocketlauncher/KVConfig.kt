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

    companion object {
        val TYPE_STRING = 0
        val TYPE_INT = 1
        val TYPE_LONG = 2
        val TYPE_FLOAT = 3
        val TYPE_BOOLEAN = 4
        val TYPE_ARRAY = 5
        val TYPE_OBJECT = 6
    }

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    val lock = ReentrantReadWriteLock()
    val file: File = File(filePath)
    val cache: MutableMap<String, Any?> = mutableMapOf()

    var isReleased: Boolean = false

    init {
        loadFromFile()
    }
    fun <T> set(key: String, value: T?) {
        if (isReleased) return
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
        if (isReleased) return defaultValue
        return lock.read {
            (cache[key] as? T) ?: defaultValue
        }
    }
    fun getString(key: String, defaultValue: String = ""): String {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            null -> defaultValue
            else -> value.toString()
        }
    }
    fun getInt(key: String, defaultValue: Int = 0): Int {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: defaultValue
            is Number -> value.toInt() != 0
            else -> defaultValue
        }
    }
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        if (isReleased) return defaultValue
        return when (val value = cache[key]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    fun <T> setObject(key: String, obj: T?) {
        if (isReleased) return
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
        if (isReleased) return null
        return lock.read {
            try {
                val json = cache[key] as? String
                json?.let { gson.fromJson(it, T::class.java) }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getArray(key: String): List<Any> {
        if (isReleased) return emptyList()

        return lock.read {
            try {
                val value = cache[key]

                when (value) {
                    is String -> {
                        val typeToken = object : TypeToken<List<Any>>() {}
                        gson.fromJson<List<Any>>(value, typeToken.type) ?: emptyList()
                    }

                    is List<*> -> {
                        value.filterNotNull().map { it as Any }
                    }

                    is Array<*> -> {
                        value.filterNotNull().map { it as Any }.toList()
                    }

                    else -> emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getArray(key: String, defaultValue: List<Any>): List<Any> {
        if (isReleased) return defaultValue

        return lock.read {
            try {
                val value = cache[key]

                when (value) {
                    is String -> {
                        val typeToken = object : TypeToken<List<Any>>() {}
                        gson.fromJson<List<Any>>(value, typeToken.type) ?: defaultValue
                    }

                    is List<*> -> {
                        value.filterNotNull().map { it as Any } ?: defaultValue
                    }

                    is Array<*> -> {
                        value.filterNotNull().map { it as Any }.toList() ?: defaultValue
                    }

                    else -> defaultValue
                }
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    fun setArray(key: String, list: List<Any>?) {
        if (isReleased) return

        if (list == null) {
            remove(key)
            return
        }

        lock.write {
            // 将列表序列化为JSON字符串存储
            cache[key] = gson.toJson(list)
            saveToFile()
        }
    }

    fun remove(key: String) {
        if (isReleased) return
        set(key, null)
    }
    fun clear() {
        if (isReleased) return
        lock.write {
            cache.clear()
            saveToFile()
        }
    }
    fun contains(key: String): Boolean {
        if (isReleased) return false
        return lock.read {
            cache.containsKey(key)
        }
    }
    fun keys(): Set<String> {
        if (isReleased) return emptySet()
        return lock.read {
            cache.keys.toSet()
        }
    }
    fun setAll(values: Map<String, Any?>) {
        if (isReleased) return
        lock.write {
            cache.putAll(values)
            values.forEach { (key, value) ->
                if (value == null) cache.remove(key)
            }
            saveToFile()
        }
    }
    fun getAll(): Map<String, Any?> {
        if (isReleased) return emptyMap()
        return lock.read {
            cache.toMap()
        }
    }
    private fun loadFromFile() {
        if (isReleased) return
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
        if (isReleased) return
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
    fun release() {
        if (isReleased) return
        isReleased = true
        lock.write {
            cache.clear()
        }
    }
    fun getType(key: String): Int? {
        if (isReleased) return null
        return when (val value = cache[key]) {
            is String -> TYPE_STRING
            is Int -> TYPE_INT
            is Long -> TYPE_LONG
            is Float -> TYPE_FLOAT
            is Boolean -> TYPE_BOOLEAN
            is Array<*> -> TYPE_ARRAY
            is List<*> -> TYPE_ARRAY
            is Map<*, *> -> TYPE_OBJECT
            else -> null
        }
    }
}