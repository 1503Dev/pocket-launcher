package dev1503.pocketlauncher

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev1503.pocketlauncher.modloader.ModInfo

open class InstanceInfo(
    val name: String?,
    val versionName: String,
    val versionCode: Long,
    val installTime: Long,
    val source: String,
    val dirPath: String,
    val entityType: String,
    val entity: String,
    val context: Context? = null,
    val initKv: Boolean = false,
) {
    lateinit var manifest: JsonObject
    lateinit var config: KVConfig

    init {
        if (context != null && initKv) {
            manifest = Gson().fromJson(
                Utils.fileReadString("$dirPath/manifest.json"),
                JsonObject::class.java
            )
            config = KVConfig(context, "$dirPath/config.json")
        }
    }

    var dataIsolation: Boolean
        get() = config.getBoolean("data_isolation", true)
        set(value) {
            config.set("data_isolation", value)
        }
    var dataStorageDir: String
        get() = config.getString("data_storage_dir", ":INSTANCE/data/")
        set(value) {
            config.set("data_storage_dir", value)
        }
    val dataStorageDirParsed: String
        get() {
            if (dataIsolation) {
                if (dataStorageDir == "") dataStorageDir = ":INSTANCE/data/"
                val dir = dataStorageDir.replace(":INSTANCE", dirPath.removeSuffix("/"))
                if (!dir.endsWith("/")) return "$dir/"
                return dir
            } else {
                return Utils.getGlobalGameStorageDataDirPath(context!!)
            }
        }
    val iconBitmap: Bitmap?
        get() {
            if (context != null && apkPath != null) {
                return Utils.drawable2Bitmap(Utils.getApkIcon(context, apkPath!!))
            }
            return null
        }
    val apkPath: String?
        get() {
            if (entityType == "full_apk" && context != null) {
                return Utils.getInstanceEntitiesDirPath(context, "apk") + entity + ".apk"
            }
            return null
        }
    val enabledModIds: List<String>
        get() {
            return config.getArray("mods", emptyList()) as List<String>
        }
    val enabledMods: List<ModInfo>
        get() {
            val ids = enabledModIds
            val pkg = ids.map { it.split(":")[0] }
            val v = ids.map { it.split(":")[1] }
            return Utils.getModsInfoByPackagesAndVersions(context!!, pkg, v)
        }
    var deviceModel: String
        get() = config.getString("device_model", "")
        set(value) {
            config.set("device_model", value)
        }
}