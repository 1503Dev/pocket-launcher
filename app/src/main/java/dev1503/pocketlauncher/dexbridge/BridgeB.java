package dev1503.pocketlauncher.dexbridge;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

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
        Log.d(TAG, "onCreate(" + bundle + ")");
    }
    public static void afterOnCreate(MinecraftActivity self, Bundle bundle) {
        Log.d(TAG, "After:onCreate(" + bundle + ")");
    }
    public static String getExternalStoragePath(MinecraftActivity self, String path) {
        String rez = utils.getDirIPath(self, "cache/mc");
        Log.d(TAG, "getExternalStoragePath(): " + path + " -> " + rez);
        return rez;
    }
    public static String getInternalStoragePath(MinecraftActivity self) {
        String rez = utils.getDirIPath(self, "cache/mc");
        Log.d(TAG, "getInternalStoragePath(): " + rez);
        return rez;
    }
    public static void onDestroy(MinecraftActivity self) {
        Log.d(TAG, "onDestroy(" + self + ")");
        System.exit(0);
    }
    public static void afterOnDestroy(MinecraftActivity self) {
        Log.d(TAG, "After:onDestroy(" + self + ")");
    }

    private static RuntimeException stub(){
        return new RuntimeException("Stub!");
    }
}

