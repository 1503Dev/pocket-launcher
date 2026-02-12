package dev1503.pocketlauncher.mod.events;

import com.mojang.minecraftpe.NetworkMonitor;

import dev1503.pocketlauncher.mod.events.types.NetworkMonitorNativeUpdateNetworkStatusParams;

public interface OnNetworkMonitorNativeUpdateNetworkStatusListener extends EventListener {
    public static final String NAME = "onNetworkMonitorNativeUpdateNetworkStatus";
    NetworkMonitorNativeUpdateNetworkStatusParams
        nativeUpdateNetworkStatus(NetworkMonitor networkMonitor,
                                  NetworkMonitorNativeUpdateNetworkStatusParams mcOriginParams,
                                  NetworkMonitorNativeUpdateNetworkStatusParams lastResult);
}
