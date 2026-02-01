package dev1503.pocketlauncher

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.JsonObject

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

    val dataIsolation: Boolean
        get() = config.getBoolean("data_isolation", true)
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
}