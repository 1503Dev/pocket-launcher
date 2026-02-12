package com.mojang.minecraftpe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.mod.events.OnNetworkMonitorNativeUpdateNetworkStatusListener;
import dev1503.pocketlauncher.mod.events.types.NetworkMonitorNativeUpdateNetworkStatusParams;
import dev1503.pocketlauncher.modloader.ModEventListener;

public class NetworkMonitor {
    public static final String TAG = "NetworkMonitor";

    public static final int NETWORK_CATEGORY_ETHERNET = 0;
    public static final int NETWORK_CATEGORY_WIFI = 1;
    public static final int NETWORK_CATEGORY_OTHER = 2;

    public final HashMap<Integer, HashSet<Network>> mAvailableNetworksPerCategory;
    public final Context mContext;

    @SuppressLint("StaticFieldLeak")
    public static NetworkMonitor INSTANCE;
    private static final ModEventListener.Companion modEventListener = ModEventListener.Companion;

    public native void nativeUpdateNetworkStatus(boolean hasEthernet, boolean hasWifi, boolean hasOther);

    public NetworkMonitor(Context context) {
        INSTANCE = this;
        mContext = context;
        Log.d(TAG, "_init(" + context + ")");
        mAvailableNetworksPerCategory = new HashMap<>();
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_ETHERNET, new HashSet<>());
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_WIFI, new HashSet<>());
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_OTHER, new HashSet<>());

        registerNetworkCallbacks();
    }

    public void registerNetworkCallbacks() {
        Log.d(TAG, "registerNetworkCallbacks()");
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_CELLULAR, NETWORK_CATEGORY_OTHER);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_WIFI, NETWORK_CATEGORY_WIFI);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH, NETWORK_CATEGORY_OTHER);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_ETHERNET, NETWORK_CATEGORY_ETHERNET);

        if (Build.VERSION.SDK_INT >= 31) {
            _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_VPN, NETWORK_CATEGORY_OTHER);
        }
    }

    public void _addNetworkCallbacksForTransport(int transportType, int networkCategory) {
        Log.d(TAG, "_addNetworkCallbacksForTransport(" + transportType + ", " + networkCategory + ")");
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = _createNetworkRequestForTransport(transportType);

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                mAvailableNetworksPerCategory.get(networkCategory).add(network);
                _updateStatus();
            }

            @Override
            public void onLost(Network network) {
                mAvailableNetworksPerCategory.get(networkCategory).remove(network);
                _updateStatus();
            }
        });
    }

    public NetworkRequest _createNetworkRequestForTransport(int transportType) {
        Log.d(TAG, "_createNetworkRequestForTransport(" + transportType + ")");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (Build.VERSION.SDK_INT >= 23) {
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        builder.addTransportType(transportType);
        return builder.build();
    }

    public void _updateStatus() {
        Log.d(TAG, "_updateStatus()");
        boolean hasEthernet = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_ETHERNET)).isEmpty();
        boolean hasWifi = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_WIFI)).isEmpty();
        boolean hasOther = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_OTHER)).isEmpty();

        Object eventResult = modEventListener.invoke(OnNetworkMonitorNativeUpdateNetworkStatusListener.NAME,
                this,
                new NetworkMonitorNativeUpdateNetworkStatusParams(false, this, hasEthernet, hasWifi, hasOther));
        if (eventResult instanceof NetworkMonitorNativeUpdateNetworkStatusParams) {
            NetworkMonitorNativeUpdateNetworkStatusParams params = (NetworkMonitorNativeUpdateNetworkStatusParams) eventResult;
            if (params.isPrevented) {
                Log.w(TAG, "nativeUpdateNetworkStatus was prevented");
                return;
            }
            hasEthernet = params.hasEthernet;
            hasWifi = params.hasWifi;
            hasOther = params.hasOther;
        }
        Log.d(TAG, "nativeUpdateNetworkStatus(" + hasEthernet + ", " + hasWifi + ", " + hasOther + ")");
        nativeUpdateNetworkStatus(hasEthernet, hasWifi, hasOther);
    }
}