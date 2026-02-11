package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityGetInternalStoragePathListener extends EventListener {
    public static final String NAME = "onMinecraftActivityGetInternalStoragePath";
    String getInternalStoragePath(MinecraftActivity activity, String mcOriginPath, String launcherOriginPath, String lastResult);
}
