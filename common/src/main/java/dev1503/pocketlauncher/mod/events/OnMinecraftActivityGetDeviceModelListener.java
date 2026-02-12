package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityGetDeviceModelListener extends EventListener {
    public static final String NAME = "onMinecraftActivityGetDeviceModel";
    String getDeviceModel(MinecraftActivity activity, String mcOriginDeviceModel, String lastResult);
}
