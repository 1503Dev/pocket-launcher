package dev1503.pocketlauncher.dexbridge;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

import java.lang.reflect.Method;
import java.util.Arrays;

import dev1503.pocketlauncher.InstanceInfo;
import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;
import dev1503.pocketlauncher.mod.events.AfterMinecraftActivityOnCreateListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetDeviceModelListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetExternalStoragePathListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetInternalStoragePathListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetLegacyDeviceIDListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityGetLegacyExternalStoragePathListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityHasWriteExternalStoragePermissionListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityOnCreateListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityOnDestroyListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityRequestStoragePermissionListener;
import dev1503.pocketlauncher.mod.events.OnMinecraftActivityStaticInitListener;
import dev1503.pocketlauncher.modloader.ModEventListener;

public class BridgeB {
    public static final String TAG = "BridgeB";
    public static final Utils utils = Utils.INSTANCE;
    public static final ModEventListener.Companion modEventListener = ModEventListener.Companion;

    public static MinecraftActivity self;
    public static BridgeA bridgeA;

    public static void _client() {
        Log.d(TAG, "<client>()");
        modEventListener.invoke(OnMinecraftActivityStaticInitListener.NAME);
    }
    public static void onCreate(MinecraftActivity activity, Bundle bundle){
        Log.d(TAG, "onCreate(" + bundle + ")");
        self = activity;
        Object eventResult = modEventListener.invoke(OnMinecraftActivityOnCreateListener.NAME, self, bundle);
        if (eventResult instanceof Boolean && ((Boolean) eventResult)) {
            return;
        }
        bridgeA = new BridgeA(self);
    }
    public static void afterOnCreate(MinecraftActivity self, Bundle bundle) {
        Log.d(TAG, "After:onCreate(" + bundle + ")");
        Object eventResult = modEventListener.invoke(AfterMinecraftActivityOnCreateListener.NAME, self, bundle);
        if (eventResult instanceof Boolean && ((Boolean) eventResult)) {
            return;
        }
        String cacheLibsDir = utils.getADirIPath(self, "cache/launcher/native_libs");
        utils.fileRemove(cacheLibsDir);
        Log.d(TAG, "Removed " + cacheLibsDir);
    }
    public static String getExternalStoragePath(MinecraftActivity self, String ori) {
        String rez = bridgeA.getDataDirPath();
        Object eventResult = modEventListener.invoke(OnMinecraftActivityGetExternalStoragePathListener.NAME, self, ori, rez);
        if (eventResult instanceof String) {
            rez = (String) eventResult;
        }
        Log.d(TAG, "getExternalStoragePath(): " + ori + " -> " + rez);
        return rez;
    }
    public static String getInternalStoragePath(MinecraftActivity self, String ori) {
        String rez = bridgeA.getDataDirPath();
        Object eventResult = modEventListener.invoke(OnMinecraftActivityGetInternalStoragePathListener.NAME, self, ori, rez);
        if (eventResult instanceof String) {
            rez = (String) eventResult;
        }
        Log.d(TAG, "getInternalStoragePath(): " + ori + " -> " + rez);
        return rez;
    }
    public static void onDestroy(MinecraftActivity self) {
        Log.d(TAG, "onDestroy()");
        Object eventResult = modEventListener.invoke(OnMinecraftActivityOnDestroyListener.NAME, self);
        if (eventResult instanceof Boolean && ((Boolean) eventResult)) {
            return;
        }
        System.exit(0);
    }
    public static void afterOnDestroy(MinecraftActivity self) {
        Log.d(TAG, "After:onDestroy()");
    }
    public static String getLegacyExternalStoragePath(MinecraftActivity self, String path, String ori) {
        Log.d(TAG, "getLegacyExternalStoragePath(" + path + "): " + ori);
        Object eventResult = modEventListener.invoke(OnMinecraftActivityGetLegacyExternalStoragePathListener.NAME, self, path, ori);
        if (eventResult instanceof String) {
            ori = (String) eventResult;
        }
        return ori;
    }
    public static String getLegacyDeviceID(MinecraftActivity self, String ori) {
        Log.d(TAG, "getLegacyDeviceID(): " + ori);
        Object eventResult = modEventListener.invoke(OnMinecraftActivityGetLegacyDeviceIDListener.NAME, self, ori);
        if (eventResult instanceof String) {
            ori = (String) eventResult;
        }
        return ori;
    }
    public static String getDeviceModel(MinecraftActivity self, String ori) {
        String rez = bridgeA.getDeviceModel();
        Object eventResult = modEventListener.invoke(OnMinecraftActivityGetDeviceModelListener.NAME, self, ori, rez);
        if (eventResult instanceof String) {
            rez = (String) eventResult;
        }
        Log.d(TAG, "getDeviceModel(): " + ori + " -> " + rez);
        return rez;
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
        Object eventResult = modEventListener.invoke(OnMinecraftActivityRequestStoragePermissionListener.NAME, self, paramInt, true);
        boolean rez = true;
        if (eventResult instanceof Boolean) {
            rez = (Boolean) eventResult;
        }
        Log.d(TAG, "requestStoragePermission(" + paramInt + "): Prevented[" + rez + "]");
        return rez;
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
        Object eventResult = modEventListener.invoke(OnMinecraftActivityHasWriteExternalStoragePermissionListener.NAME, self, ori, rez);
        if (eventResult instanceof Boolean) {
            rez = (Boolean) eventResult;
        }
        Log.d(TAG, "hasWriteExternalStoragePermission(): " + ori + " -> " + rez);
        return rez;
    }

    private static RuntimeException stub(){
        return new RuntimeException("Stub!");
    }
}

