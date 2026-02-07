package dev1503.pocketlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import dev1503.pocketlauncher.modloader.ModInfo

object Utils {
    const val TAG = "Utils"
    const val XAL_DEFAULT_CONFIG_FILE_NAME = "1734634999945796391"
    @SuppressLint("StaticFieldLeak")
    var kvGlobalGameConfig: KVConfig? = null
    var kvLauncherSettings: KVConfig? = null

    @ColorInt
    fun getColorFromAttr(context: Context, attrResId: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(attrResId, typedValue, true)) {
            typedValue.data
        } else {
            Color.BLACK
        }
    }

    fun setAllTextColor(viewGroup: ViewGroup, @ColorInt color: Int) {
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            when (view) {
                is ViewGroup -> setAllTextColor(view, color)
                is TextView -> {
                    if (view.tag != null && view.tag == "description") {
                        continue
                    }
                    view.setTextColor(color)
                }
            }
        }
    }

    fun getAppVersionName(context: Context, packageName: String): String? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
    }

    fun getAppVersionName(context: Context): String? {
        return getAppVersionName(context, context.packageName)
    }

    fun getDataDirPath(context: Context): String {
        return getADataDirPath(context) + "pocket_launcher/"
    }

    fun getADataDirPath(context: Context): String {
        return if (android.os.Build.VERSION.SDK_INT >= 24) {
            context.dataDir.absolutePath + "/"
        } else {
            context.filesDir.parentFile?.absolutePath + "/"
        }
    }

    fun getXalDirPath(context: Context): String {
        return getADataDirPath(context) + "xal/"
    }

    fun getCurrentXalIdRx(context: Context): Single<String>? {
        val path = getXalDirPath(context) + XAL_DEFAULT_CONFIG_FILE_NAME
        val file = File(path)
        return if (!file.exists()) {
            null
        } else {
            readStringFromFileRx(file)
        }
    }

    fun readStringFromFileRx(file: File): Single<String> {
        return Single.fromCallable<String> {
            file.source().use { source ->
                source.buffer().use { bufferedSource ->
                    bufferedSource.readUtf8()
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @SuppressLint("CheckResult")
    fun searchFilesWithContent(
        dirPath: String?,
        targetStr: String?,
        listener: FilesSearchWithContentListener?
    ) {
        if (dirPath == null || targetStr == null || listener == null) {
            listener?.onSearchError(IllegalArgumentException("Invalid params"))
            return
        }

        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            listener.onSearchError(IllegalArgumentException("Directory not found: $dirPath"))
            return
        }

        Single.fromCallable {
            val fileArray = dir.listFiles()
            if (fileArray == null) {
                return@fromCallable SearchResult(ArrayList(), ArrayList())
            }

            val matchedFiles = ArrayList<File>()
            val matchedContents = ArrayList<String>()

            for (file in fileArray) {
                if (!file.isFile || !file.canRead()) {
                    continue
                }

                try {
                    val content = readFileContentSync(file)
                    if (content != null && content.contains(targetStr)) {
                        matchedFiles.add(file)
                        matchedContents.add(content)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e)
                }
            }

            return@fromCallable SearchResult(matchedFiles, matchedContents)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> listener.onSearchComplete(result.files, result.contents) },
                { error -> listener.onSearchError(error) }
            )
    }

    private data class SearchResult(
        val files: List<File>,
        val contents: List<String>
    )

    private fun readFileContentSync(file: File): String? {
        return try {
            file.source().use { source ->
                source.buffer().use { bufferedSource ->
                    bufferedSource.readUtf8()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e)
            null
        }
    }

    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setTimeout(runnable: Runnable, timeout: Long) {
        Handler(Looper.getMainLooper()).postDelayed(runnable, timeout)
    }

    @ColorInt
    fun applyAlpha(@ColorInt color: Int, alpha: Float): Int {
        val a = (alpha * 255).roundToInt()
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)
    }

    fun getDrawableFromAttr(context: Context?, attrResId: Int): Drawable? {
        if (context == null) {
            return null
        }
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrResId))
        return try {
            val drawableResId = typedArray.getResourceId(0, 0)
            if (drawableResId != 0) {
                ContextCompat.getDrawable(context, drawableResId)
            } else {
                null
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun getAllMCPossiblePackages(context: Context?): List<PackageInfo>? {
        if (context == null) {
            return null
        }
        return try {
            val packageInfoList = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val mcPackages = ArrayList<PackageInfo>()
            for (packageInfo in packageInfoList) {
                if (packageInfo.packageName.startsWith("com.mojang.")) {
                    mcPackages.add(packageInfo)
                }
            }
            mcPackages
        } catch (e: Exception) {
            Log.e(TAG, e)
            null
        }
    }

    fun getInstancesDirPath(context: Context): String {
        return getDataDirPath(context) + "instances/"
    }

    fun isInstanceExist(context: Context, instanceName: String): Boolean {
        for (name in getInstanceNames(context)) {
            if (name.trim().equals(instanceName.trim(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun getInstanceNames(context: Context): List<String> {
        val instancesDir = File(getInstancesDirPath(context))
        val files = instancesDir.listFiles() ?: return ArrayList()
        val instanceNames = ArrayList<String>()
        for (file in files) {
            if (file.isDirectory) {
                instanceNames.add(file.name)
            }
        }
        return instanceNames
    }

    fun testFileName(filename: String?): Boolean {
        if (filename.isNullOrBlank()) {
            return false
        }
        if (filename.isEmpty()) {
            return false
        }
        if (filename.length > 255) {
            return false
        }

        val illegalCharPattern = Pattern.compile(
            "[\u0000-\u001F\u007F]" +
                    "|[<>:\"|?*\\\\/]" +
                    "|/" +
                    "|^\\s|\\s$" +
                    "|\\.{2,}" +
                    "|^\\." +
                    "|\\s+$|\\.+$"
        )

        if (illegalCharPattern.matcher(filename).find()) {
            return false
        }

        val upperFilename = filename.uppercase(Locale.getDefault())
        val windowsReservedPattern = Pattern.compile(
            "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$",
            Pattern.CASE_INSENSITIVE
        )

        if (windowsReservedPattern.matcher(upperFilename).matches()) {
            return false
        }

        val dangerousExtensionPattern = Pattern.compile(
            "\\.(exe|bat|cmd|com|scr|pif|vbs|js|jar|lnk|sh|bash|zsh|desktop|app)$",
            Pattern.CASE_INSENSITIVE
        )

        if (dangerousExtensionPattern.matcher(filename).find()) {
            return false
        }

        val unicodeControlPattern = Pattern.compile(
            "[\u200B-\u200F\u202A-\u202E\u2060-\u206F\uFEFF]"
        )

        if (unicodeControlPattern.matcher(filename).find()) {
            return false
        }

        val rtlPattern = Pattern.compile("[\u200E\u200F\u202A-\u202E]")
        return !rtlPattern.matcher(filename).find()
    }
    suspend fun copyFile(
        sourcePath: String?,
        destPath: String,
        onProgress: (Int, Long, Long) -> Unit
    ) {
        requireNotNull(sourcePath) { "Source path cannot be null" }

        val sourceFile = File(sourcePath)
        val destFile = File(destPath)

        if (!sourceFile.exists()) {
            throw _root_ide_package_.okio.FileNotFoundException("Source file not found: $sourcePath") as Throwable
        }

        destFile.parentFile?.mkdirs()

        val totalSize = sourceFile.length()
        var copiedSize: Long = 0
        var lastProgress = -1
        var lastUpdateTime = 0L
        val updateInterval = 50L

        sourceFile.inputStream().use { input ->
            destFile.outputStream().use { output ->
                val buffer = ByteArray(8192)

                while (true) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break

                    output.write(buffer, 0, bytesRead)
                    copiedSize += bytesRead

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= updateInterval) {
                        val progress = if (totalSize > 0) {
                            (copiedSize.toFloat() / totalSize * 100).toInt()
                        } else 0

                        if (progress != lastProgress) {
                            withContext(Dispatchers.Main.immediate) {
                                onProgress(progress, copiedSize, totalSize)
                            }
                            lastProgress = progress
                            lastUpdateTime = currentTime
                        }
                    }
                }
                withContext(Dispatchers.Main.immediate) {
                    onProgress(100, totalSize, totalSize)
                }
            }
        }
    }
    fun fileWriteString(path: String, content: String): Boolean {
        try {
            File(path).parentFile?.mkdirs()
            File(path).writeText(content)
            return true
        } catch (e: Exception) {
            Log.e(TAG, e)
            return false
        }
    }
    fun fileReadString(path: String): String? {
        try {
            return File(path).readText()
        } catch (e: Exception) {
            Log.e(TAG, e)
            return null
        }
    }
    fun getAllInstances(context: Context): List<InstanceInfo> {
        val instanceNames = getInstanceNames(context)
        val instances = ArrayList<InstanceInfo>()
        for (name in instanceNames) {
            try {
                val instanceDir = File(getInstancesDirPath(context) + name)
                val manifestFile = File(instanceDir, "manifest.json")
                val manifestJson = fileReadString(manifestFile.absolutePath) ?: continue
                val manifest = Gson().fromJson(manifestJson, JsonObject::class.java)
                val mInstance = manifest.getAsJsonObject("instance")
                val versionName = mInstance.get("version_name").asString
                val versionCode = mInstance.get("version_code").asLong
                val installTime = mInstance.get("install_time").asLong
                val source = mInstance.get("source").asString
                val entityType = mInstance.get("type").asString
                instances.add(
                    InstanceInfo(
                        name,
                        versionName,
                        versionCode,
                        installTime,
                        source,
                        instanceDir.absolutePath + "/",
                        entityType,
                        mInstance.get("entity").asString
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }
        return instances
    }
    fun getInstanceInfo(context: Context, name: String?, initKv: Boolean = false): InstanceInfo? {
        val instanceNames = getInstanceNames(context)
        if (!instanceNames.contains(name)) return null
        try {
            val instanceDir = File(getInstancesDirPath(context) + name)
            val manifestFile = File(instanceDir, "manifest.json")
            val manifestJson = fileReadString(manifestFile.absolutePath)
            val manifest = Gson().fromJson(manifestJson, JsonObject::class.java)
            val mInstance = manifest.getAsJsonObject("instance")
            val versionName = mInstance.get("version_name").asString
            val versionCode = mInstance.get("version_code").asLong
            val installTime = mInstance.get("install_time").asLong
            val source = mInstance.get("source").asString
            val entityType = mInstance.get("type").asString
            val entity = mInstance.get("entity").asString
            return InstanceInfo(
                name,
                versionName,
                versionCode,
                installTime,
                source,
                instanceDir.absolutePath + "/",
                entityType,
                entity,
                context,
                initKv
            )
        } catch (e: Exception) {
            Log.e(TAG, e)
        }
        return null
    }
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }
    fun fileSHA1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return bytesToHex(digest.digest())
    }
    fun fileSHA1(filePath: String?): String {
        return fileSHA1(File(filePath))
    }
    fun fileSHA1(file: File, progressCallback: ((progress: Float) -> Unit)? = null): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val fileSize = file.length().toFloat()
        var bytesProcessed = 0L

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesProcessed += bytesRead
                progressCallback?.invoke(bytesProcessed / fileSize)
            }
        }

        return bytesToHex(digest.digest())
    }
    fun fileSHA1(filePath: String?, progressCallback: ((Float) -> Unit)? = null): String {
        return fileSHA1(File(filePath), progressCallback)
    }
    fun getInstanceEntitiesDirPath(context: Context, type: String): String {
        return getDataDirPath(context) + "instance_entities/$type/"
    }
    fun isInstanceEntityExist(context: Context, type: String, entity: String, suffix: String = type): Boolean {
        val instanceEntitiesDir = File(getInstanceEntitiesDirPath(context, type))
        if (!instanceEntitiesDir.exists()) return false
        val entityFile = File(instanceEntitiesDir, entity + (if (suffix.isNotEmpty()) ".${suffix.lowercase()}" else ""))
        return entityFile.exists() && entityFile.isFile
    }
    fun fileRemove(file: File) {
        val path = file.absolutePath.toPath()

        if (file.isDirectory) {
            FileSystem.SYSTEM.listOrNull(path)?.forEach { childPath ->
                val childFile = File(childPath.toString())
                if (childFile.exists()) {
                    fileRemove(childFile)
                }
            }
        }
        try {
            FileSystem.SYSTEM.delete(path, mustExist = false)
        } catch (e: Exception) {
            throw e
        }
    }
    fun fileRemove(path: String) {
        fileRemove(File(path))
    }
    fun getDirIPath(context: Context, name: String): String {
        val dir = File(getDataDirPath(context), name)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/"
    }
    fun getADirIPath(context: Context, name: String): String {
        val dir = File(getADataDirPath(context), name)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/"
    }
    fun fileCopy(stream: InputStream, destPath: String) {
        val destFile = File(destPath)
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        val destStream = FileOutputStream(destFile)
        stream.copyTo(destStream)
        destStream.close()
    }
    fun getSelectedInstance(context: Context, initKv: Boolean = false): InstanceInfo? {
        return kvLauncherSettings?.getString("instance")?.let {
            getInstanceInfo(context, it, initKv)
        }
    }
    fun getApkIcon(context: Context, apkPath: String): Drawable? {
        val pm = context.packageManager
        val packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES) ?: return null

        val appInfo = packageInfo.applicationInfo
        appInfo?.sourceDir = apkPath
        appInfo?.publicSourceDir = apkPath

        return appInfo?.loadIcon(pm)
    }
    fun drawable2Bitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = drawable?.intrinsicWidth?.coerceAtLeast(1) ?: 1
        val height = drawable?.intrinsicHeight?.coerceAtLeast(1) ?: 1

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)

        return bitmap
    }
    fun getGlobalGameStorageDirPath(context: Context): String {
        return getDirIPath(context, "global")
    }
    fun getGlobalGameStorageDataDirPath(context: Context): String {
        return getGlobalGameStorageDirPath(context) + "data/"
    }
    fun getModsDirPath(context: Context): String {
        return getDirIPath(context, "mods")
    }
    fun getAllMods(context: Context): List<ModInfo> {
        val modList = mutableListOf<ModInfo>()
        val modsDir = File(getModsDirPath(context))
        if (modsDir.exists() && modsDir.isDirectory) {
            modsDir.listFiles()?.forEach { it ->
                if (it.isDirectory) {
                    if (File(it, "manifest.json").exists()) {
                        val manifest = KVConfig(context, File(it, "manifest.json").absolutePath)
                        val supportedVersions = mutableListOf<String>()
                        if (manifest.getType("supported_versions") == KVConfig.TYPE_ARRAY) {
                            manifest.getArray("supported_versions").forEach { iu ->
                                if (iu is String) supportedVersions.add(iu)
                            }
                        } else if (manifest.getType("supported_versions") == KVConfig.TYPE_STRING) {
                            supportedVersions.add(manifest.getString("supported_versions", ""))
                        }
                        val modInfo = ModInfo(
                            manifest.getString("name", ""),
                            manifest.getString("package", ""),
                            supportedVersions,
                            it.absolutePath + "/",
                            manifest.getString("loader", ""),
                            manifest.getString("entry", ""),
                            manifest.getString("entry_method", "")
                        )
                        if (modInfo.packageName.isNotEmpty() && modInfo.loader.isNotEmpty() && modInfo.entry.isNotEmpty()) {
                            modList.add(modInfo)
                        }
                    }
                }
            }
        }
        return modList
    }
    fun getModsSupported(context: Context, version: String): List<ModInfo> {
        return getAllMods(context).filter { it.isVersionSupported(version) }
    }

    interface FilesSearchWithContentListener {
        fun onSearchComplete(files: List<File>, fileContents: List<String>)
        fun onSearchError(error: Throwable)
    }
}