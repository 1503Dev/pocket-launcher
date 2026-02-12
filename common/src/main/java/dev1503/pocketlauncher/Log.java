package dev1503.pocketlauncher;

import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Log {
    private static final String TAG_PREFIX = "PL::";

    public static final int ASSERT = android.util.Log.ASSERT;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int INFO = android.util.Log.INFO;
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int WARN = android.util.Log.WARN;

    public static String logHistoryFormatted = "";

    private Log() {
        throw new RuntimeException("Stub!");
    }

    public static String time(){
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
        return sdf.format(currentTime);
    }
    public static String getTypeString(int type){
        switch (type){
            case ASSERT: return "A";
            case DEBUG: return "D";
            case ERROR: return "E";
            case INFO: return "I";
            case VERBOSE: return "V";
            case WARN: return "W";
            default: return "U";
        }
    }

    public static int assertColor = Color.parseColor("#FF0000");
    public static int verboseColor = Color.parseColor("#EEEEEE");
    public static int infoColor = Color.parseColor("#0FDCB6");
    public static int warnColor = Color.parseColor("#FFA800");
    public static int errorColor = Color.parseColor("#D81765");
    public static int debugColor = Color.parseColor("#FFFFFF");
    public static int timeColor = Color.parseColor("#ADD8E6");

    private static final Map<String, Integer> tagColorCache = new HashMap<>();

    public static void write(int type, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        String time = time();
        String typeStr = getTypeString(type);
        String firstLinePrefix = time + " " + typeStr + " [" + tag + "] ";

        if (msg == null) {
            msg = "";
        }

        if (tr != null) {
            msg = msg + (msg.isEmpty() ? "" : "\n") + getStackTraceString(tr);
        }

        String[] lines = msg.split("\n", -1);
        StringBuilder fullLog = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                fullLog.append(firstLinePrefix).append(lines[i]).append("\n");
            } else {
                StringBuilder spaces = new StringBuilder();
                for (int j = 0; j < firstLinePrefix.length(); j++) {
                    spaces.append(" ");
                }
                fullLog.append(spaces.toString()).append(lines[i]).append("\n");
            }
        }

        String newText = logHistoryFormatted + fullLog.toString();
        SpannableString spannable = new SpannableString(newText);

        int start = 0;
        int newTextLength = newText.length();

        int currentLogTypeColor = 0;
        char typeChar = typeStr.charAt(0);
        switch (typeChar) {
            case 'W': currentLogTypeColor = warnColor; break;
            case 'E': currentLogTypeColor = errorColor; break;
            case 'D': currentLogTypeColor = debugColor; break;
            case 'I': currentLogTypeColor = infoColor; break;
            case 'V': currentLogTypeColor = verboseColor; break;
            case 'A': currentLogTypeColor = assertColor; break;
        }

        while (start < newTextLength) {
            int lineEnd = newText.indexOf('\n', start);
            if (lineEnd == -1) lineEnd = newTextLength;

            String currentLine = newText.substring(start, lineEnd);
            int lineLength = currentLine.length();

            if (lineLength > 0) {
                boolean isTimeStampedLine = false;
                int currentTypeColor = 0;

                if (lineLength >= 12) {
                    if (lineLength > 13) {
                        char firstChar = currentLine.charAt(13);
                        isTimeStampedLine = firstChar == 'W' || firstChar == 'E' || firstChar == 'D' ||
                                firstChar == 'I' || firstChar == 'V' || firstChar == 'A';

                        switch (firstChar) {
                            case 'W': currentTypeColor = warnColor; break;
                            case 'E': currentTypeColor = errorColor; break;
                            case 'D': currentTypeColor = debugColor; break;
                            case 'I': currentTypeColor = infoColor; break;
                            case 'V': currentTypeColor = verboseColor; break;
                            case 'A': currentTypeColor = assertColor; break;
                        }
                    }
                }

                if (isTimeStampedLine && currentTypeColor != 0) {
                    spannable.setSpan(new ForegroundColorSpan(timeColor),
                            start, start + 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannable.setSpan(new ForegroundColorSpan(currentTypeColor),
                            start + 12, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    int tagStart = currentLine.indexOf('[');
                    int tagEnd = currentLine.indexOf(']');
                    if (tagStart != -1 && tagEnd != -1) {
                        String tagText = currentLine.substring(tagStart + 1, tagEnd);
                        Integer tagCol = tagColorCache.get(tagText);
                        if (tagCol == null) {
                            tagCol = generateColorForTag(tagText);
                            tagColorCache.put(tagText, tagCol);
                        }
                        spannable.setSpan(new ForegroundColorSpan(tagCol),
                                start + tagStart, start + tagEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    if (currentLine.startsWith(" ".repeat(firstLinePrefix.length()))) {
                        if (currentLogTypeColor != 0) {
                            spannable.setSpan(new ForegroundColorSpan(currentLogTypeColor),
                                    start, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        int lineColor = 0;
                        if (lineLength > 13) {
                            char firstChar = currentLine.charAt(13);
                            switch (firstChar) {
                                case 'W': lineColor = warnColor; break;
                                case 'E': lineColor = errorColor; break;
                                case 'D': lineColor = debugColor; break;
                                case 'I': lineColor = infoColor; break;
                                case 'V': lineColor = verboseColor; break;
                                case 'A': lineColor = assertColor; break;
                            }
                        }

                        if (lineColor != 0) {
                            spannable.setSpan(new ForegroundColorSpan(lineColor),
                                    start, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
            }

            start = lineEnd + 1;
        }

        logHistoryFormatted = spannable.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (logHistoryFormatted.lines().count() > 100) {
                logHistoryFormatted = deleteLines(logHistoryFormatted, 100);
            }
        }
        if (GlobalDebugWindow.instance != null) {
            GlobalDebugWindow.instance.setLoggerText(spannable);
        }
    }

    public static String deleteLines(String input, int keepLines) {
        String[] lines = input.split("\r?\n");

        if (lines.length <= keepLines) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        int startIndex = lines.length - keepLines;

        for (int i = startIndex; i < lines.length; i++) {
            result.append(lines[i]);
            if (i < lines.length - 1) {
                result.append(System.lineSeparator());
            }
        }

        return result.toString();
    }

    private static int findTypeColorForContinuationLine(String text, int continuationLineStart) {
        int searchPos = continuationLineStart - 1;
        if (searchPos < 0) return 0;

        int prevNewline = text.lastIndexOf('\n', searchPos - 1);
        if (prevNewline == -1) prevNewline = 0;
        else prevNewline++;

        String prevLine = text.substring(prevNewline, continuationLineStart - 1);

        if (prevLine.length() > 13) {
            char firstChar = prevLine.charAt(13);
            switch (firstChar) {
                case 'W': return warnColor;
                case 'E': return errorColor;
                case 'D': return debugColor;
                case 'I': return infoColor;
                case 'V': return verboseColor;
                case 'A': return assertColor;
            }
        }

        return 0;
    }

    private static int generateColorForTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return Color.LTGRAY;
        }

        int hash = tag.hashCode();

        hash = Math.abs(hash);

        float hue = (hash % 360) / 360.0f;
        float saturation = 0.7f;
        float lightness = 0.65f;

        return hslToColor(hue, saturation, lightness);
    }

    private static int hslToColor(float h, float s, float l) {
        float r, g, b;

        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f/3f);
        }

        return Color.rgb((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f/6f) return p + (q - p) * 6f * t;
        if (t < 1f/2f) return q;
        if (t < 2f/3f) return p + (q - p) * (2f/3f - t) * 6f;
        return p;
    }

    @NonNull
    public static String getStackTraceString(@Nullable Throwable tr) {
        assert tr != null;
        return Utils.INSTANCE.stackTraceToString(tr);
    }

    public static int d(@Nullable String tag, @NonNull String msg) {
        write(DEBUG, tag, msg, null);
        return android.util.Log.d(TAG_PREFIX + tag, msg);
    }

    public static int d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(DEBUG, tag, msg, tr);
        return android.util.Log.d(TAG_PREFIX + tag, msg, tr);
    }

    public static int e(@Nullable String tag, @NonNull String msg) {
        write(ERROR, tag, msg, null);
        return android.util.Log.e(TAG_PREFIX + tag, msg);
    }

    public static int e(@Nullable String tag, @NonNull Throwable tr) {
        write(ERROR, tag, null, tr);
        return android.util.Log.e(TAG_PREFIX + tag, "", tr);
    }

    public static int e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(ERROR, tag, msg, tr);
        return android.util.Log.e(TAG_PREFIX + tag, msg, tr);
    }

    public static int i(@Nullable String tag, @NonNull String msg) {
        write(INFO, tag, msg, null);
        return android.util.Log.i(TAG_PREFIX + tag, msg);
    }

    public static int i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(INFO, tag, msg, tr);
        return android.util.Log.i(TAG_PREFIX + tag, msg, tr);
    }

    public static boolean isLoggable(@Nullable String tag, int level) {
        return android.util.Log.isLoggable(TAG_PREFIX + tag, level);
    }

    public static int println(int priority, @Nullable String tag, @NonNull String msg) {
        write(priority, tag, msg, null);
        return android.util.Log.println(priority, TAG_PREFIX + tag, msg);
    }

    public static int v(@Nullable String tag, @NonNull String msg) {
        write(VERBOSE, tag, msg, null);
        return android.util.Log.v(TAG_PREFIX + tag, msg);
    }

    public static int v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(VERBOSE, tag, msg, tr);
        return android.util.Log.v(TAG_PREFIX + tag, msg, tr);
    }

    public static int w(@Nullable String tag, @NonNull String msg) {
        write(WARN, tag, msg, null);
        return android.util.Log.w(TAG_PREFIX + tag, msg);
    }

    public static int w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(WARN, tag, msg, tr);
        return android.util.Log.w(TAG_PREFIX + tag, msg, tr);
    }

    public static int w(@Nullable String tag, @Nullable Throwable tr) {
        write(WARN, tag, null, tr);
        return android.util.Log.w(TAG_PREFIX + tag, tr);
    }

    public static int wtf(@Nullable String tag, @Nullable String msg) {
        write(ASSERT, tag, msg, null);
        return android.util.Log.wtf(TAG_PREFIX + tag, msg);
    }

    public static int wtf(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        write(ASSERT, tag, msg, tr);
        return android.util.Log.wtf(TAG_PREFIX + tag, msg, tr);
    }

    public static int wtf(@Nullable String tag, @NonNull Throwable tr) {
        write(ASSERT, tag, null, tr);
        return android.util.Log.wtf(TAG_PREFIX + tag, tr);
    }
}