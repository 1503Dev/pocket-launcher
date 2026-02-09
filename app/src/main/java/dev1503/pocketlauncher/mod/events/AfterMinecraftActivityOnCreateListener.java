package dev1503.pocketlauncher.mod.events;

import android.os.Bundle;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface AfterMinecraftActivityOnCreateListener extends EventListener {
    public static final String NAME = "afterMinecraftActivityOnCreate";
    boolean onCreate(MinecraftActivity activity, Bundle savedInstanceState, boolean isPrevented);
}
