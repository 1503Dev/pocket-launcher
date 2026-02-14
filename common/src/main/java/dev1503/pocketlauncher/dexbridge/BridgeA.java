package dev1503.pocketlauncher.dexbridge;

import dev1503.pocketlauncher.InstanceInfo;
import dev1503.pocketlauncher.KVConfig;
import dev1503.pocketlauncher.Log;
import dev1503.pocketlauncher.Utils;

public class BridgeA {
    private final String TAG = "BridgeA";

    private final InstanceInfo instanceInfo;

    private final KVConfig kvGlobalGameConfig;
    private final KVConfig kvLauncherSettings;

    private final Utils utils = Utils.INSTANCE;
    private MinecraftActivity self;

    public BridgeA(MinecraftActivity self) {
        this.self = self;
        this.kvGlobalGameConfig = new KVConfig(self, utils.getGlobalGameStorageDirPath(self) + "config.json");
        this.kvLauncherSettings = new KVConfig(self, utils.getDataDirPath(self) + "launcher_settings.json");

        utils.setKvLauncherSettings(kvLauncherSettings);
        utils.setKvGlobalGameConfig(kvGlobalGameConfig);

        InstanceInfo selectedInstance = utils.getSelectedInstance(self, true);
        if (selectedInstance == null) {
            selectedInstance = new InstanceInfo(null, "", 0, 0, "", "", "", "",  "", null, false);
        }

        this.instanceInfo = selectedInstance;

        Log.i(TAG, "Init");
    }
    public String getDataDirPath() {
        return instanceInfo.getDataStorageDirParsed();
    }
    public String getDeviceModel() {
        String deviceModel = instanceInfo.getDeviceModel();
        if (deviceModel.isEmpty()) {
            deviceModel = kvGlobalGameConfig.getString("device_model", utils.getDeviceModelName());
        }
        return deviceModel;
    }
}