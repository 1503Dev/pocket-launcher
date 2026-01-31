package dev1503.pocketlauncher.dexbridge;

import android.content.res.AssetManager;
import android.os.Bundle;

import java.lang.reflect.Method;

import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;

public class BridgeB {
    public static final String TAG = "BridgeB";
    public static final Utils utils = Utils.INSTANCE;

    public static MinecraftActivity self;

    public static void _client() {
        Log.d(TAG, "<client>()");
    }
    public static void onCreate(MinecraftActivity activity, Bundle bundle){
        self = activity;
        Log.d(TAG, "onCreate(" + self + ", " + bundle + ")");
        AssetManager am = self.getAssets();
        try {
            Method addAssetPath = am.getClass().getDeclaredMethod(
                    "addAssetPath", String.class);
            addAssetPath.invoke(am, BridgeA.getApkPath());
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }
    public static String getExternalStoragePath() {
        String rez = utils.getDirIPath(self, "cache/mc");
        Log.d(TAG, "getExternalStoragePath() = " + rez);
        return rez;
    }

    private static RuntimeException stub(){
        return new RuntimeException("Stub!");
    }
}

