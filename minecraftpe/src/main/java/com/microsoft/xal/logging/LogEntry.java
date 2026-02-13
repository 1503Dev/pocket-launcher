package com.microsoft.xal.logging;

public class LogEntry {
    private final XalLogger.LogLevel level;
    private final String message;

    public LogEntry(XalLogger.LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public String Message() {
        return message;
    }

    public int Level() {
        return level.toInt();
    }
}