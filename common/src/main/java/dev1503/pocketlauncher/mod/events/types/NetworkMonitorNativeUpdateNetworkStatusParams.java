package dev1503.pocketlauncher.mod.events.types;

import com.mojang.minecraftpe.NetworkMonitor;

public class NetworkMonitorNativeUpdateNetworkStatusParams extends MethodParams {
    public final NetworkMonitor networkMonitor;
    public boolean hasEthernet;
    public boolean hasWifi;
    public boolean hasOther;

    public NetworkMonitorNativeUpdateNetworkStatusParams(boolean isPrevented, NetworkMonitor networkMonitor, boolean hasEthernet, boolean hasWifi, boolean hasOther) {
        super(isPrevented);
        this.networkMonitor = networkMonitor;
        this.hasEthernet = hasEthernet;
        this.hasWifi = hasWifi;
        this.hasOther = hasOther;
    }
}
