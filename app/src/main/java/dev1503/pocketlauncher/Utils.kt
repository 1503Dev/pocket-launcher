package dev1503.pocketlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
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
import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

object Utils {
    const val TAG = "Utils"
    const val XAL_DEFAULT_CONFIG_FILE_NAME = "1734634999945796391"
    var kvConfig: KVConfig? = null

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
        return if (android.os.Build.VERSION.SDK_INT >= 24) {
            context.dataDir.absolutePath + "/"
        } else {
            context.filesDir.parentFile?.absolutePath + "/"
        }
    }

    fun getXalDirPath(context: Context): String {
        return getDataDirPath(context) + "xal/"
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
        val sourceFile = File(sourcePath)
        val destFile = File(destPath)

        if (!sourceFile.exists()) {
            throw _root_ide_package_.okio.FileNotFoundException("Source file not found: $sourcePath") as Throwable
        }

        destFile.parentFile?.mkdirs()

        val totalSize = sourceFile.length()
        var copiedSize: Long = 0

        sourceFile.source().use { source ->
            destFile.sink().use { sink ->
                val buffer = Buffer()

                while (true) {
                    val bytesRead = source.read(buffer, 8192)
                    if (bytesRead == -1L) break

                    sink.write(buffer, bytesRead)
                    copiedSize += bytesRead

                    val progress = if (totalSize > 0) {
                        (copiedSize.toFloat() / totalSize * 100).toInt()
                    } else 0

                    withContext(Dispatchers.Main){
                        onProgress(progress, copiedSize, totalSize)
                    }
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
                val entityRelativePath = mInstance.get("entity").asString
                instances.add(
                    InstanceInfo(
                        name,
                        versionName,
                        versionCode,
                        installTime,
                        source,
                        instanceDir.absolutePath + "/",
                        entityType,
                        entityRelativePath
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }
        return instances
    }
    fun getInstanceInfo(context: Context, name: String?): InstanceInfo? {
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
            val entityRelativePath = mInstance.get("entity").asString
            return InstanceInfo(
                name,
                versionName,
                versionCode,
                installTime,
                source,
                instanceDir.absolutePath + "/",
                entityType,
                entityRelativePath
            )
        } catch (e: Exception) {
            Log.e(TAG, e)
        }
        return null
    }

    interface FilesSearchWithContentListener {
        fun onSearchComplete(files: List<File>, fileContents: List<String>)
        fun onSearchError(error: Throwable)
    }
}