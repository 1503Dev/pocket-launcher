package dev1503.pocketlauncher.dexbridge;

public class BridgeA {
    private static String apkPath;

    public static void setApkPath(String path){
        apkPath = path;
    };
    public static String getApkPath(){
        return apkPath;
    };
}
