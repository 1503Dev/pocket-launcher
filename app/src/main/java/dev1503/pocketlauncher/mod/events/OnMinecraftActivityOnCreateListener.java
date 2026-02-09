package dev1503.pocketlauncher.mod.events;

import android.os.Bundle;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityOnCreateListener extends EventListener {
    public static final String NAME = "onMinecraftActivityOnCreate";
    boolean onCreate(MinecraftActivity activity, Bundle savedInstanceState, boolean isPrevented);
}
