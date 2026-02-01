package dev1503.pocketlauncher.dexbridge;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

import java.lang.reflect.Method;
import java.util.Arrays;

import dev1503.pocketlauncher.InstanceInfo;
import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;

public class BridgeB {
    public static final String TAG = "BridgeB";
    public static final Utils utils = Utils.INSTANCE;

    public static MinecraftActivity self;
    public static BridgeA bridgeA;

    public static void _client() {
        Log.d(TAG, "<client>()");
    }
    public static void onCreate(MinecraftActivity activity, Bundle bundle){
        Log.d(TAG, "onCreate(" + bundle + ")");
        self = activity;
        bridgeA = new BridgeA(self);
    }
    public static void afterOnCreate(MinecraftActivity self, Bundle bundle) {
        Log.d(TAG, "After:onCreate(" + bundle + ")");
        String cacheLibsDir = utils.getDirIPath(self, "cache/launcher/native_libs");
        utils.fileRemove(cacheLibsDir);
        Log.d(TAG, "Removed " + cacheLibsDir);
    }
    public static String getExternalStoragePath(MinecraftActivity self, String ori) {
        String rez = bridgeA.getDataDirPath();
        Log.d(TAG, "getExternalStoragePath(): " + ori + " -> " + rez);
        return rez;
    }
    public static String getInternalStoragePath(MinecraftActivity self, String ori) {
        String rez = bridgeA.getDataDirPath();
        Log.d(TAG, "getInternalStoragePath(): " + ori + " -> " + rez);
        return rez;
    }
    public static void onDestroy(MinecraftActivity self) {
        Log.d(TAG, "onDestroy()");
        System.exit(0);
    }
    public static void afterOnDestroy(MinecraftActivity self) {
        Log.d(TAG, "After:onDestroy()");
    }
    public static String getLegacyExternalStoragePath(MinecraftActivity self, String path, String ori) {
        String rez = bridgeA.getDataDirPath();
        Log.d(TAG, "getLegacyExternalStoragePath(" + path + "): " + ori + " -> " + rez);
        return rez;
    }
    public static String getLegacyDeviceID(MinecraftActivity self, String ori) {
        Log.d(TAG, "getLegacyDeviceID(): " + ori);
        return ori;
    }
    public static String getDeviceModel(MinecraftActivity self, String ori) {
        Log.d(TAG, "getDeviceModel(): " + ori);
        return ori;
    }
    public static int getAndroidVersion(MinecraftActivity self, int ori) {
        Log.d(TAG, "getAndroidVersion(): " + ori);
        return ori;
    }
    public static int getDisplayHeight(MinecraftActivity self, int ori) {
        Log.d(TAG, "getDisplayHeight(): " + ori);
        return ori;
    }
    public static int getDisplayWidth(MinecraftActivity self, int ori) {
        Log.d(TAG, "getDisplayWidth(): " + ori);
        return ori;
    }
    public static int getScreenWidth(MinecraftActivity self, int ori) {
        Log.d(TAG, "getScreenWidth(): " + ori);
        return ori;
    }
    public static int getScreenHeight(MinecraftActivity self, int ori) {
        Log.d(TAG, "getScreenHeight(): " + ori);
        return ori;
    }
    public static boolean requestStoragePermission(MinecraftActivity self, int paramInt) {
        Log.d(TAG, "requestStoragePermission(" + paramInt + "): ");
        return true;
    }
    public static boolean requestPushPermission(MinecraftActivity self) {
        Log.d(TAG, "requestPushPermission()");
        return true;
    }
    public static boolean onRequestPermissionsResult(MinecraftActivity self, int paramInt, String[] paramArrayOfString, int[] paramArrayOfint) {
        Log.d(TAG, "onRequestPermissionsResult(" + paramInt + ", " + Arrays.toString(paramArrayOfString) + ", " + Arrays.toString(paramArrayOfint) + ")");
        return true;
    }
    public static boolean hasWriteExternalStoragePermission(MinecraftActivity self, boolean ori) {
        boolean rez = false;
        Log.d(TAG, "hasWriteExternalStoragePermission(): " + ori + " -> " + rez);
        return rez;
    }

    private static RuntimeException stub(){
        return new RuntimeException("Stub!");
    }
}

