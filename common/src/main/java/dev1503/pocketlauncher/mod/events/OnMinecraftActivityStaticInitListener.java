package dev1503.pocketlauncher.mod.events;

import android.os.Bundle;

import dev1503.pocketlauncher.dexbridge.MinecraftActivity;

public interface OnMinecraftActivityStaticInitListener extends EventListener {
    public static final String NAME = "onMinecraftActivityStaticInit";
    boolean _client();
}
