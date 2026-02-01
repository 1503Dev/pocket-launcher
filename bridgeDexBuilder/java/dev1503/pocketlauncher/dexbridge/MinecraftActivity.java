package dev1503.pocketlauncher.dexbridge;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import dev1503.pocketlauncher.dexbridge.BridgeB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import com.mojang.minecraftpe.MainActivity;

public class MinecraftActivity extends MainActivity {
    @Override
    public void onCreate(Bundle bundle) {
        BridgeB.onCreate(this, bundle);
        super.onCreate(bundle);
        BridgeB.afterOnCreate(this, bundle);
    }

    static {
        BridgeB._client();
    }

    @Override
    public String getExternalStoragePath() {
        return BridgeB.getExternalStoragePath(this, super.getExternalStoragePath());
    }

    @Override
    public String getInternalStoragePath() {
        return BridgeB.getInternalStoragePath(this, super.getInternalStoragePath());
    }

    @Override
    public String getLegacyExternalStoragePath(String string) {
        return BridgeB.getLegacyExternalStoragePath(this, string, super.getLegacyExternalStoragePath(string));
    }

    @Override
    public void onDestroy() {
        BridgeB.onDestroy(this);
        super.onDestroy();
        BridgeB.afterOnDestroy(this);
    }

    @Override
    public String getLegacyDeviceID() {
        return BridgeB.getLegacyDeviceID(this, super.getLegacyDeviceID());
    }

    @Override
    public String getDeviceModel() {
        return BridgeB.getDeviceModel(this, super.getDeviceModel());
    }

    @Override
    public int getAndroidVersion() {
        return BridgeB.getAndroidVersion(this, super.getAndroidVersion());
    }

    @Override
    public int getDisplayHeight() {
        return BridgeB.getDisplayHeight(this, super.getDisplayHeight());
    }

    @Override
    public int getDisplayWidth() {
        return BridgeB.getDisplayWidth(this, super.getDisplayWidth());
    }

    @Override
    public int getScreenWidth() {
        return BridgeB.getScreenWidth(this, super.getScreenWidth());
    }

    @Override
    public int getScreenHeight() {
        return BridgeB.getScreenHeight(this, super.getScreenHeight());
    }

    @Override
    public void requestStoragePermission(int paramInt) {
        if (!BridgeB.requestStoragePermission(this, paramInt))
            super.requestStoragePermission(paramInt);
    }

    public void requestPushPermission() {
        if (!BridgeB.requestPushPermission(this))
            super.requestPushPermission();
    }

    public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfint) {
        if (!BridgeB.onRequestPermissionsResult(this, paramInt, paramArrayOfString, paramArrayOfint))
            super.onRequestPermissionsResult(paramInt, paramArrayOfString, paramArrayOfint);
    }

    public boolean hasWriteExternalStoragePermission() {
        return BridgeB.hasWriteExternalStoragePermission(this, super.hasWriteExternalStoragePermission());
    }
}