package com.microsoft.xal.logging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import dev1503.pocketlauncher.Log;

public class XalLogger implements AutoCloseable {
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final String tag = "Xal";
    private final String subArea;
    private final ArrayList<LogEntry> logs = new ArrayList<>();
    private LogLevel leastVerboseLevel = LogLevel.Verbose;

    private static native void nativeLogBatch(int level, LogEntry[] logEntries);

    public enum LogLevel {
        Error(1, 'E'),
        Warning(2, 'W'),
        Important(3, 'P'),
        Information(4, 'I'),
        Verbose(5, 'V');

        private final char levelChar;
        private final int value;

        LogLevel(int value, char levelChar) {
            this.value = value;
            this.levelChar = levelChar;
        }

        public int toInt() {
            return value;
        }

        public char toChar() {
            return levelChar;
        }
    }

    public XalLogger(String subArea) {
        this.subArea = subArea;
        Verbose("XalLogger created.");
    }

    @Override
    public void close() {
        Flush();
    }

    public synchronized void Flush() {
        if (logs.isEmpty()) {
            return;
        }
        try {
            int leastVerboseLevelInt = leastVerboseLevel.toInt();
            ArrayList<LogEntry> logsCopy = logs;
            nativeLogBatch(leastVerboseLevelInt, logsCopy.toArray(new LogEntry[logsCopy.size()]));
            logs.clear();
            leastVerboseLevel = LogLevel.Verbose;
        } catch (Exception e) {
            Log.e(tag, "Failed to flush logs: " + e.toString());
        } catch (UnsatisfiedLinkError e2) {
            Log.e(tag, "Failed to flush logs: " + e2.toString());
        }
    }

    public synchronized void log(LogLevel logLevel, String message) {
        logs.add(new LogEntry(logLevel, String.format("[%c][%s][%s] %s",
                Character.valueOf(logLevel.toChar()), timestamp(), subArea, message)));
        if (leastVerboseLevel.toInt() > logLevel.toInt()) {
            leastVerboseLevel = logLevel;
        }
    }

    public void Error(String message) {
        Log.e(tag, String.format("[%s] %s", subArea, message));
        log(LogLevel.Error, message);
    }

    public void Warning(String message) {
        Log.w(tag, String.format("[%s] %s", subArea, message));
        log(LogLevel.Warning, message);
    }

    public void Important(String message) {
        Log.w(tag, String.format("[%c][%s] %s", Character.valueOf(LogLevel.Important.toChar()), subArea, message));
        log(LogLevel.Important, message);
    }

    public void Information(String message) {
        Log.i(tag, String.format("[%s] %s", subArea, message));
        log(LogLevel.Information, message);
    }

    public void Verbose(String message) {
        Log.v(tag, String.format("[%s] %s", subArea, message));
        log(LogLevel.Verbose, message);
    }

    private String timestamp() {
        return logDateFormat.format(GregorianCalendar.getInstance().getTime());
    }
}