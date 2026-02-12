package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityHasWriteExternalStoragePermissionListener extends EventListener {
    public static final String NAME = "onMinecraftActivityHasWriteExternalStoragePermission";
    boolean hasWriteExternalStoragePermission(MinecraftActivity activity, boolean mcOrigin, boolean launcherOrigin, boolean hasPermission);
}
