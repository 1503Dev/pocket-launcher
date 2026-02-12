package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityRequestStoragePermissionListener extends EventListener {
    public static final String NAME = "onMinecraftActivityRequestStoragePermission";
    boolean requestStoragePermission(MinecraftActivity activity, int paramInt, boolean defaultIsPrevented, boolean isPrevented);
}
