package dev1503.pocketlauncher.mod.events;

import android.os.Bundle;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityOnDestroyListener extends EventListener {
    public static final String NAME = "onMinecraftActivityOnDestroy";
    boolean onDestroy(MinecraftActivity activity, boolean isPrevented);
}
