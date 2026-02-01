package dev1503.pocketlauncher.dexbridge;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import dev1503.pocketlauncher.dexbridge.BridgeB;

import java.io.File;
import java.io.FileOutputStream;
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
    public String getInternalStoragePath() {
        return BridgeB.getInternalStoragePath(this);
    }
    @Override
    public void onDestroy() {
        BridgeB.onDestroy(this);
        super.onDestroy();
        BridgeB.afterOnDestroy(this);
    }
}