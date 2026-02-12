package com.mojang.minecraftpe;

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

public class NetworkMonitor {
    public static final String TAG = "NetworkMonitor";

    private static final int NETWORK_CATEGORY_ETHERNET = 0;
    private static final int NETWORK_CATEGORY_WIFI = 1;
    private static final int NETWORK_CATEGORY_OTHER = 2;

    private final HashMap<Integer, HashSet<Network>> mAvailableNetworksPerCategory;
    private final Context mContext;

    private native void nativeUpdateNetworkStatus(boolean hasEthernet, boolean hasWifi, boolean hasOther);

    public NetworkMonitor(Context context) {
        Log.i(TAG, "_init(" + context + ")");
        mContext = context;
        mAvailableNetworksPerCategory = new HashMap<>();
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_ETHERNET, new HashSet<>());
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_WIFI, new HashSet<>());
        mAvailableNetworksPerCategory.put(NETWORK_CATEGORY_OTHER, new HashSet<>());

        registerNetworkCallbacks();
        Log.i(TAG, "After::_init(" + context + ")");
    }

    private void registerNetworkCallbacks() {
        Log.i(TAG, "registerNetworkCallbacks()");
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_CELLULAR, NETWORK_CATEGORY_OTHER);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_WIFI, NETWORK_CATEGORY_WIFI);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH, NETWORK_CATEGORY_OTHER);
        _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_ETHERNET, NETWORK_CATEGORY_ETHERNET);

        if (Build.VERSION.SDK_INT >= 31) {
            _addNetworkCallbacksForTransport(NetworkCapabilities.TRANSPORT_VPN, NETWORK_CATEGORY_OTHER);
        }
        Log.i(TAG, "After::registerNetworkCallbacks()");
    }

    private void _addNetworkCallbacksForTransport(int transportType, int networkCategory) {
        Log.i(TAG, "_addNetworkCallbacksForTransport(" + transportType + ", " + networkCategory + ")");
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
        Log.i(TAG, "After::_addNetworkCallbacksForTransport(" + transportType + ", " + networkCategory + ")");
    }

    private NetworkRequest _createNetworkRequestForTransport(int transportType) {
        Log.i(TAG, "_createNetworkRequestForTransport(" + transportType + ")");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (Build.VERSION.SDK_INT >= 23) {
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        builder.addTransportType(transportType);
        return builder.build();
    }

    private void _updateStatus() {
        Log.i(TAG, "_updateStatus()");
        boolean hasEthernet = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_ETHERNET)).isEmpty();
        boolean hasWifi = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_WIFI)).isEmpty();
        boolean hasOther = !Objects.requireNonNull(mAvailableNetworksPerCategory.get(NETWORK_CATEGORY_OTHER)).isEmpty();

        nativeUpdateNetworkStatus(hasEthernet, hasWifi, hasOther);
        Log.i(TAG, "After::_updateStatus()");
    }
}