package dev1503.pocketlauncher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Log {
    private static final String TAG_PREFIX = "PL::";

    public static final int ASSERT = android.util.Log.ASSERT;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int INFO = android.util.Log.INFO;
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int WARN = android.util.Log.WARN;

    private Log() {
        throw new RuntimeException("Stub!");
    }

    public static int d(@Nullable String tag, @NonNull String msg) {
        return android.util.Log.d(TAG_PREFIX + tag, msg);
    }

    public static int d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.d(TAG_PREFIX + tag, msg, tr);
    }

    public static int e(@Nullable String tag, @NonNull String msg) {
        return android.util.Log.e(TAG_PREFIX + tag, msg);
    }

    public static int e(@Nullable String tag, @NonNull Throwable tr) {
        return e(TAG_PREFIX + tag, "", tr);
    }

    public static int e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.e(TAG_PREFIX + tag, msg, tr);
    }

    @NonNull
    public static String getStackTraceString(@Nullable Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static int i(@Nullable String tag, @NonNull String msg) {
        return android.util.Log.i(TAG_PREFIX + tag, msg);
    }

    public static int i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.i(TAG_PREFIX + tag, msg, tr);
    }

    public static boolean isLoggable(@Nullable String tag, int level) {
        return android.util.Log.isLoggable(TAG_PREFIX + tag, level);
    }

    public static int println(int priority, @Nullable String tag, @NonNull String msg) {
        return android.util.Log.println(priority, TAG_PREFIX + tag, msg);
    }

    public static int v(@Nullable String tag, @NonNull String msg) {
        return android.util.Log.v(TAG_PREFIX + tag, msg);
    }

    public static int v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.v(TAG_PREFIX + tag, msg, tr);
    }

    public static int w(@Nullable String tag, @NonNull String msg) {
        return android.util.Log.w(TAG_PREFIX + tag, msg);
    }

    public static int w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.w(TAG_PREFIX + tag, msg, tr);
    }

    public static int w(@Nullable String tag, @Nullable Throwable tr) {
        return android.util.Log.w(TAG_PREFIX + tag, tr);
    }

    public static int wtf(@Nullable String tag, @Nullable String msg) {
        return android.util.Log.wtf(TAG_PREFIX + tag, msg);
    }

    public static int wtf(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        return android.util.Log.wtf(TAG_PREFIX + tag, msg, tr);
    }

    public static int wtf(@Nullable String tag, @NonNull Throwable tr) {
        return android.util.Log.wtf(TAG_PREFIX + tag, tr);
    }
}