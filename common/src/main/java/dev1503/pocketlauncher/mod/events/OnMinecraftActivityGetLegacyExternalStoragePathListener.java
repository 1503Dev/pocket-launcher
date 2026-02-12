package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityGetLegacyExternalStoragePathListener extends EventListener {
    public static final String NAME = "onMinecraftActivityGetLegacyExternalStoragePath";
    String getLegacyExternalStoragePath(MinecraftActivity activity, String mcOriginPath, String lastResult);
}
