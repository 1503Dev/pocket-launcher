package dev1503.pocketlauncher.mod.events;

import android.os.Bundle;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityGetExternalStoragePathListener extends EventListener {
    public static final String NAME = "onMinecraftActivityGetExternalStoragePath";
    String getExternalStoragePath(MinecraftActivity activity, String mcOriginPath, String launcherOriginPath, String lastResult);
}
