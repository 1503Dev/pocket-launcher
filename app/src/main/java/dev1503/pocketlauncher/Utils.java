package dev1503.pocketlauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class Utils {
    public static final String TAG = "Utils";
    public static final String XAL_DEFAULT_CONFIG_FILE_NAME = "1734634999945796391";

    public static @ColorInt int getColorFromAttr(Context context, int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return Color.BLACK;
    }
    public static void setAllTextColor(ViewGroup viewGroup, @ColorInt int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                setAllTextColor((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                if (view.getTag() != null && view.getTag().equals("description")) {
                    continue;
                }
                ((TextView) view).setTextColor(color);
            }
        }
    }
    public static String getAppVersionName(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception e) {
            return null;
        }
    }
    public static String getAppVersionName(Context context) {
        return getAppVersionName(context, context.getPackageName());
    }
    public static String getXalDirPath(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            return context.getDataDir().getAbsolutePath() + "/xal/";
        }
        return Objects.requireNonNull(context.getFilesDir().getParentFile()).getAbsolutePath() + "/xal/";
    }
    public static Single<String> getCurrentXalIdRx(Context context) {
        String path = getXalDirPath(context) + XAL_DEFAULT_CONFIG_FILE_NAME;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return readStringFromFileRx(file);
    }
    public static Single<String> readStringFromFileRx(File file) {
        return Single.fromCallable(() -> {
                    try (Source source = Okio.source(file);
                         BufferedSource bufferedSource = Okio.buffer(source)) {
                        return bufferedSource.readUtf8();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    @SuppressLint("CheckResult")
    public static void searchFilesWithContent(String dirPath, String targetStr,
                                             FilesSearchWithContentListener listener) {
        if (dirPath == null || targetStr == null || listener == null) {
            if (listener != null) {
                listener.onSearchError(new IllegalArgumentException("Invalid params"));
            }
            return;
        }

        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            listener.onSearchError(new IllegalArgumentException("Directory not found: " + dirPath));
            return;
        }

        Single.fromCallable(() -> {
                    File[] fileArray = dir.listFiles();
                    if (fileArray == null) {
                        return new SearchResult(new ArrayList<>(), new ArrayList<>());
                    }

                    List<File> matchedFiles = new ArrayList<>();
                    List<String> matchedContents = new ArrayList<>();

                    for (File file : fileArray) {
                        if (!file.isFile() || !file.canRead()) {
                            continue;
                        }

                        try {
                            String content = readFileContentSync(file);
                            if (content != null && content.contains(targetStr)) {
                                matchedFiles.add(file);
                                matchedContents.add(content);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e);
                        }
                    }

                    return new SearchResult(matchedFiles, matchedContents);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> listener.onSearchComplete(result.files, result.contents),
                        listener::onSearchError
                );
    }
    private static class FileContentPair {
        final File file;
        final String content;

        FileContentPair(File file, String content) {
            this.file = file;
            this.content = content;
        }
    }
    private static class SearchResult {
        final List<File> files;
        final List<String> contents;

        SearchResult(List<File> files, List<String> contents) {
            this.files = files;
            this.contents = contents;
        }
    }
    private static String readFileContentSync(File file) {
        try (Source source = Okio.source(file);
             BufferedSource bufferedSource = Okio.buffer(source)) {
            return bufferedSource.readUtf8();
        } catch (Exception e) {
            Log.e(TAG, e);
            return null;
        }
    }
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static void setTimeout(Runnable runnable, long timeout) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, timeout);
    }

    public interface FilesSearchWithContentListener {
        void onSearchComplete(List<File> files, List<String> fileContents);
        void onSearchError(Throwable error);
    }
}
