package dev1503.pocketlauncher.mod.events;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityGetLegacyDeviceIDListener extends EventListener {
    public static final String NAME = "onMinecraftActivityGetLegacyDeviceID";
    String getLegacyDeviceID(MinecraftActivity activity, String mcOriginDeviceID, String lastResult);
}
