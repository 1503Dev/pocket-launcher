package dev1503.pocketlauncher.mod;

import com.mojang.minecraftpe.NetworkMonitor;

import java.lang.reflect.Field;
import java.util.Objects;

import dev1503.pocketlauncher.InstanceInfo;
import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;
import dev1503.pocketlauncher.mod.events.EventListener;
import dev1503.pocketlauncher.modloader.ModEventListener;
import dev1503.pocketlauncher.modloader.ModInfo;
import dev1503.pocketlauncher.modloader.ModLoader;

public class PocketLauncher {
    public static final String TAG = "PocketLauncher";

    private InstanceInfo instanceInfo;
    private ModInfo modInfo;
    private int loadingIndex;

    public PocketLauncher(InstanceInfo instanceInfo, ModInfo modInfo, int loadingIndex) {
        if (!Utils.INSTANCE.verifyMethodCaller(ModLoader.class, "invokeEntryMethod")) {
            Log.e(TAG, "Illegal instance creation by " + Utils.INSTANCE.getMethodCaller());
            throw new RuntimeException("Illegal instance creation");
        }

        this.instanceInfo = instanceInfo;
        this.modInfo = modInfo;
        this.loadingIndex = loadingIndex;

        Log.i(TAG, "Init by " + modInfo.getName() + "(" + modInfo.getId() + "), loadingIndex: " + loadingIndex);
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
    public int getLoadingIndex() {
        return loadingIndex;
    }
    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }
    public NetworkMonitor getNetworkMonitor() {
        try {
            Field field = NetworkMonitor.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            return (NetworkMonitor) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean listen(EventListener listener) {
        Log.d(TAG, "Mod " + modInfo.getName() + " add listener " + listener.getClass().getSimpleName());
        return ModEventListener.Companion.addListener(new ModEventListener(modInfo, listener));
    }
}
